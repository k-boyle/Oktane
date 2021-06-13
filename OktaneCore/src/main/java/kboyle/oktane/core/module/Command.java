package kboyle.oktane.core.module;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.CommandUtils;
import kboyle.oktane.core.module.callback.CommandCallback;
import kboyle.oktane.core.results.precondition.PreconditionResult;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Represents a command.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class Command {
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
    public final int optionalStart;
    public final Optional<Method> originalMethod;

    Command(CommandModule module, Builder builder) {
        Preconditions.checkState(!Strings.isNullOrEmpty(builder.name), "A command name must be a non-empty value");
        Preconditions.checkNotNull(builder.commandCallback, "builder.commandCallback cannot be null");

        var aliases = ImmutableSet.copyOf(builder.aliases);
        Preconditions.checkState(
        !aliases.isEmpty() || !module.groups.isEmpty(),
            "A command must have a non-empty alias if there are no module groups"
        );

        this.name = builder.name;
        this.aliases = aliases;
        this.description = Optional.ofNullable(builder.description);
        this.commandCallback = builder.commandCallback;
        this.preconditions = ImmutableList.copyOf(builder.preconditions);
        this.module = module;
        this.synchronised = builder.synchronised;
        this.priority = builder.priority;

        var parameters = builder.parameters;
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

        int optionalStart = -1;
        for (int i = 0; i < builtParameters.size(); i++) {
            var currentOptional = builtParameters.get(i).optional;
            if (!currentOptional) {
                continue;
            }

            Preconditions.checkState(
                i == builtParameters.size() - 1 || builtParameters.get(i + 1).optional,
                "An optional parameter cannot be followed by a non-optional one"
            );

            if (optionalStart == -1) {
                optionalStart = i;
            }
        }

        this.optionalStart = optionalStart;

        this.signature = new Signature(
            !builtParameters.isEmpty() && builtParameters.get(builtParameters.size() - 1).remainder,
            builtParameters.stream()
                .map(parameter -> parameter.type)
                .map(Class::toString)
                .collect(Collectors.joining(";"))
        );

        this.parameters = ImmutableList.copyOf(builtParameters);
        this.originalMethod = Optional.ofNullable(builder.originalMethod);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Runs the preconditions that belong to this command.
     *
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

        public final Set<String> aliases;
        public final List<CommandParameter.Builder> parameters;
        public final List<Precondition> preconditions;

        private String name;
        private String description;
        private CommandCallback commandCallback;
        private boolean synchronised;
        private int priority;
        private Method originalMethod;

        private Builder() {
            this.aliases = new HashSet<>();
            this.parameters = new ArrayList<>();
            this.preconditions = new ArrayList<>();
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

        public Builder withParameter(Consumer<CommandParameter.Builder> builderConsumer) {
            Preconditions.checkNotNull(builderConsumer, "builderConsumer cannot be null");
            var builder = CommandParameter.builder();
            builderConsumer.accept(builder);
            this.parameters.add(builder);
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

        public Builder withOriginalMethod(Method originalMethod) {
            this.originalMethod = originalMethod;
            return this;
        }

        Command build(CommandModule module) {
            return new Command(module, this);
        }
    }
}
