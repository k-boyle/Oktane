package kboyle.oktane.reactive.module;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.results.precondition.PreconditionResult;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents a command.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ReactiveCommand {
    private final String name;
    private final ImmutableSet<String> aliases;
    private final Optional<String> description;
    private final ReactiveCommandCallback commandCallback;
    private final ImmutableList<ReactiveCommandParameter> parameters;
    private final ImmutableList<ReactivePrecondition> preconditions;
    private final Signature signature;
    private final ReactiveModule module;
    private final boolean synchronised;
    private final int priority;

    ReactiveCommand(
            String name,
            ImmutableSet<String> aliases,
            Optional<String> description,
            ReactiveCommandCallback commandCallback,
            List<ReactiveCommandParameter.Builder> parameters,
            ImmutableList<ReactivePrecondition> preconditions,
            ReactiveModule module,
            boolean synchronised,
            int priority) {
        this.name = name;
        this.aliases = aliases;
        this.description = description;
        this.commandCallback = commandCallback;
        this.preconditions = preconditions;
        this.module = module;
        this.synchronised = synchronised;
        this.priority = priority;

        List<ReactiveCommandParameter> builtParameters = new ArrayList<>();
        for (int i = 0; i < parameters.size(); i++) {
            ReactiveCommandParameter commandParameter = parameters.get(i).build(this);
            Preconditions.checkState(
                !commandParameter.remainder() || i == parameters.size() - 1,
                "Parameter %s (%d) of Command %s cannot be remainder only the final parameter can be remainder",
                commandParameter.name(),
                i,
                name
            );
            builtParameters.add(commandParameter);
        }

        this.signature = new Signature(
            !builtParameters.isEmpty() && builtParameters.get(builtParameters.size() - 1).remainder(),
            builtParameters.stream()
                .map(ReactiveCommandParameter::type)
                .map(Class::toString)
                .collect(Collectors.joining(";"))
        );

        this.parameters = ImmutableList.copyOf(builtParameters);
    }

    static Builder builder() {
        return new Builder();
    }

    /**
     * Runs the preconditions that belong to this command.
     * @param context The context to pass to the preconditions.
     * @return The result of executing the preconditions.
     */
    public Mono<PreconditionResult> runPreconditions(CommandContext context) {
        return module.runPreconditions(context, this)
            .flatMap(result -> {
                if (!result.success()) {
                    return Mono.just(result);
                }

                return CommandUtil.runPreconditions(context, this, preconditions);
            });
    }

    /**
     * @return The command's name.
     */
    public String name() {
        return name;
    }

    /**
     * @return The command's aliases.
     */
    public ImmutableSet<String> aliases() {
        return aliases;
    }

    /**
     * @return The command's description.
     */
    public Optional<String> description() {
        return description;
    }

    /**
     * @return The method of the command.
     */
    public ReactiveCommandCallback commandCallback() {
        return commandCallback;
    }

    /**
     * @return The command's parameters.
     */
    public ImmutableList<ReactiveCommandParameter> parameters() {
        return parameters;
    }

    /**
     * @return The command's preconditions.
     */
    public ImmutableList<ReactivePrecondition> preconditions() {
        return preconditions;
    }

    /**
     * @return The command's signature that's used to determine uniqueness.
     */
    public Signature signature() {
        return signature;
    }

    /**
     * @return The Module that this command belongs to.
     */
    public ReactiveModule module() {
        return module;
    }

    /**
     * @return Whether the execution of the command is synchronised or not.
     */
    public boolean synchronised() {
        return synchronised;
    }

    /**
     * @return The commands priority within a given module.
     */
    public int priority() {
        return priority;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("name", name)
            .toString();
    }

    static class Builder {
        private static final String SPACE = " ";

        private final List<ReactiveCommandParameter.Builder> parameters;
        private final ImmutableSet.Builder<String> aliases;
        private final ImmutableList.Builder<ReactivePrecondition> preconditions;

        private String name;
        private String description;
        private ReactiveCommandCallback commandCallback;
        private boolean synchronised;
        private int priority;

        private Builder() {
            this.parameters = new ArrayList<>();
            this.aliases = ImmutableSet.builder();
            this.preconditions = ImmutableList.builder();
        }

        public Builder withName(String name) {
            Preconditions.checkNotNull(name, "name cannot be null");
            this.name = name;
            return this;
        }

        public Builder withAlias(String alias) {
            Preconditions.checkNotNull(alias, "alias cannot be null");
            Preconditions.checkState(!alias.contains(SPACE), "Alias %s contains a space", alias);
            this.aliases.add(alias);
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withCallback(ReactiveCommandCallback commandCallback) {
            Preconditions.checkNotNull(commandCallback, "commandCallback cannot be null");
            this.commandCallback = commandCallback;
            return this;
        }

        public Builder withParameter(ReactiveCommandParameter.Builder commandParameter) {
            Preconditions.checkNotNull(commandParameter, "commandParameter cannot be null");
            this.parameters.add(commandParameter);
            return this;
        }

        public Builder withPrecondition(ReactivePrecondition precondition) {
            Preconditions.checkNotNull(precondition, "precondition cannot be null");
            this.preconditions.add(precondition);
            return this;
        }

        public Builder synchronised() {
            this.synchronised = true;
            return this;
        }

        public Builder withSynchronised(boolean synchronised) {
            this.synchronised = synchronised;
            return this;
        }

        public Builder withPriority(int priority) {
            this.priority = priority;
            return this;
        }

        ReactiveCommand build(ReactiveModule module) {
            Preconditions.checkNotNull(name, "A command name must be specified");
            Preconditions.checkNotNull(commandCallback, "A command callback must be specified");

            ImmutableSet<String> builtAliases = this.aliases.build();
            Preconditions.checkState(
                isValidAliases(builtAliases, module.groups()),
                "A command must have a non-empty alias if there are no module groups"
            );

            return new ReactiveCommand(
                name,
                builtAliases,
                Optional.ofNullable(description),
                commandCallback,
                parameters,
                preconditions.build(),
                module,
                synchronised,
                priority);
        }

        private static boolean isValidAliases(ImmutableSet<String> commandAliases, ImmutableSet<String> moduleGroups) {
            return !commandAliases.isEmpty() || !moduleGroups.isEmpty();
        }
    }

    public static record Signature(boolean remainder, String parameters) {
    }
}
