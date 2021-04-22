package kboyle.oktane.reactive.module;

import com.google.common.base.Preconditions;
import kboyle.oktane.reactive.parsers.ReactiveTypeParser;

import java.util.Optional;

/**
 * Represents a parameter of a command.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class ReactiveCommandParameter {
    public final Class<?> type;
    public final Optional<String> description;
    public final String name;
    public final boolean remainder;
    public final ReactiveTypeParser<?> parser;
    public final ReactiveCommand command;

    ReactiveCommandParameter(
            Class<?> type,
            Optional<String> description,
            String name,
            boolean remainder,
            ReactiveTypeParser<?> parser,
            ReactiveCommand command) {
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
        private ReactiveTypeParser<?> parser;

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

        public Builder withParser(ReactiveTypeParser<?> parser) {
            this.parser = parser;
            return this;
        }

        public ReactiveCommandParameter build(ReactiveCommand command) {
            Preconditions.checkNotNull(type, "A parameter type must be specified");
            Preconditions.checkNotNull(name, "A parameter name must be specified");
            return new ReactiveCommandParameter(type, Optional.ofNullable(description), name, remainder, parser, command);
        }
    }
}
