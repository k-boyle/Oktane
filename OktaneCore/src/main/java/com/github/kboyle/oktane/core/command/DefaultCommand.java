package com.github.kboyle.oktane.core.command;

import com.github.kboyle.oktane.core.execution.CommandCallback;
import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.precondition.Precondition;
import com.github.kboyle.oktane.core.result.precondition.PreconditionResult;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.lang.reflect.Method;
import java.util.*;

public class DefaultCommand extends AbstractCommandComponent implements Command {
    private final List<Precondition> preconditions;
    private final List<CommandParameter<?>> parameters;
    private final Set<String> aliases;
    private final CommandCallback callback;
    private final CommandModule module;
    private final CommandSignature signature;
    private final Method originalMethod;
    private final boolean synchronised;
    private final int priority;

    protected DefaultCommand(Builder builder) {
        super(builder);

        this.preconditions = List.copyOf(builder.preconditions);
        var parameterBuilders = builder.parameters;
        var builtParameters = new ArrayList<CommandParameter<?>>();
        for (var i = 0; i < parameterBuilders.size(); i++) {
            var parameterBuilder = Preconditions.checkNotNull(parameterBuilders.get(i), "parameter %d cannot be null", i);
            var parameter = parameterBuilder.build(this);

            Preconditions.checkState(
                !parameter.remainder() || i == parameterBuilders.size() - 1,
                "Parameter %s (#%d) of Command %s cannot be remainder, only the final parameter can be",
                parameter.name(),
                i,
                name()
            );

            builtParameters.add(parameter);
        }

        for (int i = 0; i < builtParameters.size(); i++) {
            var currentOptional = builtParameters.get(i).optional();
            if (!currentOptional) {
                continue;
            }

            Preconditions.checkState(
                i == builtParameters.size() - 1 || builtParameters.get(i + 1).optional(),
                "An optional parameter cannot be followed by a non-optional one"
            );
        }

        this.parameters = List.copyOf(builtParameters);
        this.aliases = Set.copyOf(builder.aliases);
        this.module = Preconditions.checkNotNull(builder.module, "module cannot be null");
        this.callback = Preconditions.checkNotNull(builder.callback, "callback cannot be null");
        this.originalMethod = builder.originalMethod;
        this.synchronised = builder.synchronised;
        this.priority = builder.priority;
        this.signature = new DefaultCommandSignature(parameters, preconditions);
    }

    @Override
    public List<Precondition> preconditions() {
        return preconditions;
    }

    @Override
    public List<CommandParameter<?>> parameters() {
        return parameters;
    }

    @Override
    public Set<String> aliases() {
        return aliases;
    }

    @Override
    public CommandModule module() {
        return module;
    }

    @Override
    public CommandCallback callback() {
        return callback;
    }

    @Override
    public CommandSignature signature() {
        return signature;
    }

    @Override
    public Optional<Method> originalMethod() {
        return Optional.ofNullable(originalMethod);
    }

    @Override
    public boolean synchronised() {
        return synchronised;
    }

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public int compareTo(Command other) {
        return Integer.compare(priority(), other.priority());
    }

    public PreconditionResult runPreconditions(CommandContext context) {
        var moduleResult = module().runPreconditions(context, this);

        if (!moduleResult.success()) {
            return moduleResult;
        }

        return CommandUtil.runPreconditions(preconditions(), context, this);
    }

    protected static class Builder extends AbstractCommandComponent.Builder<Builder> implements Command.Builder {
        private final List<Precondition> preconditions;
        private final Set<String> aliases;
        private final List<CommandParameter.Builder<?>> parameters;

        private CommandCallback callback;
        private CommandModule module;
        private Method originalMethod;
        private boolean synchronised;
        private int priority;

        public Builder() {
            this.preconditions = new ArrayList<>();
            this.aliases = new HashSet<>();
            this.parameters = new ArrayList<>();
        }

        @Override
        public Builder precondition(Precondition precondition) {
            preconditions.add(Preconditions.checkNotNull(precondition, "precondition cannot be null"));
            return this;
        }

        @Override
        public Builder callback(CommandCallback callback) {
            this.callback = Preconditions.checkNotNull(callback, "callback cannot be null");
            return this;
        }

        @Override
        public Builder parameter(CommandParameter.Builder<?> parameter) {
            parameters.add(Preconditions.checkNotNull(parameter, "builder cannot be null"));
            return this;
        }

        @Override
        public Builder alias(String alias) {
            Preconditions.checkState(!Strings.isNullOrEmpty(alias), "alias must not be null or empty");
            aliases.add(alias);
            return this;
        }

        @Override
        public Builder module(CommandModule module) {
            this.module = Preconditions.checkNotNull(module, "module cannot be null");
            return this;
        }

        @Override
        public Builder originalMethod(Method originalMethod) {
            this.originalMethod = Preconditions.checkNotNull(originalMethod, "originalMethod cannot be null");
            return this;
        }

        @Override
        public Builder synchronised(boolean synchronised) {
            this.synchronised = synchronised;
            return this;
        }

        @Override
        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public Command build(CommandModule module) {
            module(module);
            return new DefaultCommand(this);
        }

        @Override
        public List<Precondition> preconditions() {
            return preconditions;
        }

        @Override
        public CommandCallback callback() {
            return callback;
        }

        @Override
        public List<CommandParameter.Builder<?>> parameters() {
            return parameters;
        }

        @Override
        public Set<String> aliases() {
            return aliases;
        }

        @Override
        public CommandModule module() {
            return module;
        }

        @Override
        public Method originalMethod() {
            return originalMethod;
        }

        @Override
        public boolean synchronised() {
            return synchronised;
        }

        @Override
        public int priority() {
            return priority;
        }
    }
}
