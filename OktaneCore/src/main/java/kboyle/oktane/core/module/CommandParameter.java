package kboyle.oktane.core.module;

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

    CommandParameter(
            Class<?> type,
            Optional<String> description,
            String name,
            boolean remainder,
            TypeParser<?> parser,
            Command command) {
        this.type = type;
        this.description = description;
        this.name = name;
        this.remainder = remainder;
        this.parser = parser;
        this.command = command;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Class<?> type;
        private String description;
        private String name;
        private boolean remainder;
        private TypeParser<?> parser;

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

        public CommandParameter build(Command command) {
            Preconditions.checkNotNull(type, "A parameter type must be specified");
            Preconditions.checkNotNull(name, "A parameter name must be specified");
            return new CommandParameter(type, Optional.ofNullable(description), name, remainder, parser, command);
        }
    }
}
