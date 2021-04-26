package kboyle.oktane.core.module;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.CommandUtils;
import kboyle.oktane.core.module.callback.CommandCallback;
import kboyle.oktane.core.results.precondition.PreconditionResult;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents a command.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class Command {
    public final String name;
    public final ImmutableSet<String> aliases;
    public final Optional<String> description;
    public final CommandCallback commandCallback;
    public final ImmutableList<CommandParameter> parameters;
    public final ImmutableList<Precondition> preconditions;
    public final Signature signature;
    public final CommandModule module;
    public final boolean synchronised;
    public final int priority;

    Command(
            String name,
            ImmutableSet<String> aliases,
            Optional<String> description,
            CommandCallback commandCallback,
            List<CommandParameter.Builder> parameters,
            ImmutableList<Precondition> preconditions,
            CommandModule module,
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

        var builtParameters = new ArrayList<CommandParameter>();
        for (var i = 0; i < parameters.size(); i++) {
            var commandParameter = parameters.get(i).build(this);
            Preconditions.checkState(
                !commandParameter.remainder || i == parameters.size() - 1,
                "Parameter %s (%d) of Command %s cannot be remainder only the final parameter can be remainder",
                commandParameter.name,
                i,
                name
            );
            builtParameters.add(commandParameter);
        }

        this.signature = new Signature(
            !builtParameters.isEmpty() && builtParameters.get(builtParameters.size() - 1).remainder,
            builtParameters.stream()
                .map(parameter -> parameter.type)
                .map(Class::toString)
                .collect(Collectors.joining(";"))
        );

        this.parameters = ImmutableList.copyOf(builtParameters);
    }

    public static Builder builder() {
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

                return CommandUtils.runPreconditions(context, this, preconditions);
            });
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("name", name)
            .toString();
    }

    public static class Builder {
        private static final String SPACE = " ";

        private final List<CommandParameter.Builder> parameters;
        private final ImmutableSet.Builder<String> aliases;
        private final ImmutableList.Builder<Precondition> preconditions;

        private String name;
        private String description;
        private CommandCallback commandCallback;
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

        public Builder withCallback(CommandCallback commandCallback) {
            Preconditions.checkNotNull(commandCallback, "commandCallback cannot be null");
            this.commandCallback = commandCallback;
            return this;
        }

        public Builder withParameter(CommandParameter.Builder commandParameter) {
            Preconditions.checkNotNull(commandParameter, "commandParameter cannot be null");
            this.parameters.add(commandParameter);
            return this;
        }

        public Builder withPrecondition(Precondition precondition) {
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

        Command build(CommandModule module) {
            Preconditions.checkNotNull(name, "A command name must be specified");
            Preconditions.checkNotNull(commandCallback, "A command callback must be specified");

            var builtAliases = this.aliases.build();
            Preconditions.checkState(
                isValidAliases(builtAliases, module.groups),
                "A command must have a non-empty alias if there are no module groups"
            );

            return new Command(
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
