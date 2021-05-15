package kboyle.oktane.core.module;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import kboyle.oktane.core.parsers.TypeParser;

import java.util.Optional;

/**
 * Represents a parameter of a command.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class CommandParameter {
    public final Class<?> type;
    public final Optional<String> description;
    public final String name;
    public final boolean remainder;
    public final TypeParser<?> parser;
    public final Command command;
    public final boolean optional;
    public final Optional<String> defaultValue;

    CommandParameter(
            Class<?> type,
            Optional<String> description,
            String name,
            boolean remainder,
            TypeParser<?> parser,
            Command command,
            boolean optional,
            Optional<String> defaultValue) {
        this.type = type;
        this.description = description;
        this.name = name;
        this.remainder = remainder;
        this.parser = parser;
        this.command = command;
        this.optional = optional;
        this.defaultValue = defaultValue;
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
        private Class<?> type;
        private String description;
        private String name;
        private boolean remainder;
        private TypeParser<?> parser;
        private boolean optional;
        private String defaultValue;

        private Builder() {
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

        public CommandParameter build(Command command) {
            Preconditions.checkNotNull(type, "A parameter type must be specified");
            Preconditions.checkNotNull(name, "A parameter name must be specified");
            return new CommandParameter(
                type,
                Optional.ofNullable(description),
                name,
                remainder,
                parser,
                command,
                optional,
                Optional.ofNullable(defaultValue)
            );
        }
    }
}
