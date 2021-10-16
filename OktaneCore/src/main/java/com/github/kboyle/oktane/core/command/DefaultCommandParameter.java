package com.github.kboyle.oktane.core.command;

import com.github.kboyle.oktane.core.parsing.TypeParser;
import com.github.kboyle.oktane.core.precondition.ParameterPrecondition;
import com.google.common.base.Preconditions;

import java.lang.reflect.Parameter;
import java.util.*;

public class DefaultCommandParameter<T> extends AbstractCommandComponent implements CommandParameter<T> {
    private final List<ParameterPrecondition<T>> preconditions;
    private final String defaultString;
    private final Parameter originalParameter;
    private final Class<T> type;
    private final TypeParser<T> typeParser;
    private final Command command;
    private final boolean remainder;
    private final boolean optional;
    private final boolean varargs;
    private final boolean greedy;

    // todo be consistent about which builder is accepted
    protected DefaultCommandParameter(DefaultCommandParameter.Builder<T> builder) {
        super(builder);

        this.preconditions = List.copyOf(builder.preconditions);
        this.defaultString = builder.defaultString;
        this.originalParameter = builder.originalParameter;
        this.type = Preconditions.checkNotNull(builder.type, "type cannot be null");
        this.typeParser = Preconditions.checkNotNull(builder.typeParser, "typeParser cannot be null");
        this.command = Preconditions.checkNotNull(builder.command, "command cannot be null");
        this.remainder = builder.remainder;
        this.optional = builder.optional;
        this.varargs = builder.varargs;
        this.greedy = builder.greedy;
    }

    @Override
    public List<ParameterPrecondition<T>> preconditions() {
        return preconditions;
    }

    @Override
    public Optional<String> defaultString() {
        return Optional.ofNullable(defaultString);
    }

    @Override
    public Optional<Parameter> originalParameter() {
        return Optional.ofNullable(originalParameter);
    }

    @Override
    public Class<T> type() {
        return type;
    }

    @Override
    public TypeParser<T> typeParser() {
        return typeParser;
    }

    @Override
    public Command command() {
        return command;
    }

    @Override
    public boolean remainder() {
        return remainder;
    }

    @Override
    public boolean optional() {
        return optional;
    }

    @Override
    public boolean varargs() {
        return varargs;
    }

    @Override
    public boolean greedy() {
        return greedy;
    }

    protected static class Builder<T> extends AbstractCommandComponent.Builder<Builder<T>> implements CommandParameter.Builder<T> {
        private final List<ParameterPrecondition<T>> preconditions;

        private String defaultString;
        private Parameter originalParameter;
        private Class<T> type;
        private TypeParser<T> typeParser;
        private Command command;
        private boolean remainder;
        private boolean optional;
        private boolean varargs;
        private boolean greedy;

        public Builder() {
            this.preconditions = new ArrayList<>();
        }

        @Override
        public Builder<T> precondition(ParameterPrecondition<T> precondition) {
            this.preconditions.add(Preconditions.checkNotNull(precondition, "precondition cannot be null"));
            return this;
        }

        @Override
        public Builder<T> defaultString(String defaultString) {
            this.defaultString = Preconditions.checkNotNull(defaultString, "defaultString cannot be null");
            optional();
            return this;
        }

        @Override
        public Builder<T> originalParameter(Parameter originalParameter) {
            this.originalParameter = Preconditions.checkNotNull(originalParameter, "originalParameter cannot be null");
            return this;
        }

        @Override
        public Builder<T> type(Class<T> type) {
            this.type = Preconditions.checkNotNull(type, "type cannot be null");
            return this;
        }

        @Override
        public Builder<T> typeParser(TypeParser<T> typeParser) {
            this.typeParser = Preconditions.checkNotNull(typeParser, "typeParser cannot be null");
            return this;
        }

        @Override
        public Builder<T> command(Command command) {
            this.command = command;
            return this;
        }

        @Override
        public Builder<T> remainder(boolean remainder) {
            this.remainder = remainder;
            return this;
        }

        @Override
        public Builder<T> optional(boolean optional) {
            this.optional = optional;
            return this;
        }

        @Override
        public Builder<T> varargs(boolean varargs) {
            this.varargs = varargs;
            return this;
        }

        @Override
        public Builder<T> greedy(boolean greedy) {
            this.greedy = greedy;
            return this;
        }

        @Override
        public CommandParameter<T> build(Command command) {
            command(command);
            return new DefaultCommandParameter<>(this);
        }

        @Override
        protected Builder<T> self() {
            return this;
        }

        @Override
        public List<ParameterPrecondition<T>> preconditions() {
            return preconditions;
        }

        @Override
        public String defaultString() {
            return defaultString;
        }

        @Override
        public Parameter originalParameter() {
            return originalParameter;
        }

        @Override
        public Class<T> type() {
            return type;
        }

        @Override
        public TypeParser<T> typeParser() {
            return typeParser;
        }

        @Override
        public Command command() {
            return command;
        }

        @Override
        public boolean remainder() {
            return remainder;
        }

        @Override
        public boolean optional() {
            return optional;
        }

        @Override
        public boolean varargs() {
            return varargs;
        }

        @Override
        public boolean greedy() {
            return greedy;
        }
    }
}
