package kboyle.oktane.core.module;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import kboyle.oktane.core.parsers.TypeParser;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents a parameter of a command.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class CommandParameter {
    public final String name;
    public final Class<?> type;
    public final Optional<String> description;
    public final boolean remainder;
    public final TypeParser<?> parser;
    public final Command command;
    public final boolean optional;
    public final Optional<String> defaultValue;
    public final Optional<Parameter> originalParameter;
    public final ImmutableList<Precondition> preconditions;

    CommandParameter(Command command, Builder builder) {
        Preconditions.checkState(!Strings.isNullOrEmpty(builder.name), "A parameter name must be a non-empty value");
        Preconditions.checkNotNull(builder.type, "builder.type cannot be null");

        this.name = builder.name;
        this.type = builder.type;
        this.description = Optional.ofNullable(builder.description);
        this.remainder = builder.remainder;
        this.parser = builder.parser;
        this.command = command;
        this.optional = builder.optional;
        this.defaultValue = Optional.ofNullable(builder.defaultValue);
        this.originalParameter = Optional.ofNullable(builder.originalParameter);
        this.preconditions = ImmutableList.copyOf(builder.preconditions);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("name", name)
            .toString();
    }

    public static class Builder {
        public final List<Precondition> preconditions;

        private Class<?> type;
        private String description;
        private String name;
        private boolean remainder;
        private TypeParser<?> parser;
        private boolean optional;
        private String defaultValue;
        private Parameter originalParameter;

        private Builder() {
            this.preconditions = new ArrayList<>();
        }

        public Builder withType(Class<?> type) {
            Preconditions.checkNotNull(type, "type cannot be null");
            this.type = type;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withName(String name) {
            Preconditions.checkNotNull(name, "name cannot be null");
            this.name = name;
            return this;
        }

        public Builder withRemainder(boolean remainder) {
            this.remainder = remainder;
            return this;
        }

        public Builder withParser(TypeParser<?> parser) {
            Preconditions.checkNotNull(parser, "parser cannot be null");
            this.parser = parser;
            return this;
        }

        public Builder withOptional(boolean optional) {
            this.optional = optional;
            return this;
        }

        public Builder optional() {
            this.optional = true;
            return this;
        }

        public Builder withDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue == null || defaultValue.isEmpty() ? null : defaultValue;
            this.optional = true;
            return this;
        }

        public Builder withOriginalParameter(Parameter originalParameter) {
            Preconditions.checkNotNull(originalParameter, "originalParameter cannot be null");
            this.originalParameter = originalParameter;
            return this;
        }

        public Builder withPrecondition(Precondition precondition) {
            Preconditions.checkNotNull(precondition, "precondition cannot be null");
            precondition.validate(type);
            this.preconditions.add(precondition);
            return this;
        }

        CommandParameter build(Command command) {
            return new CommandParameter(command, this);
        }
    }
}
