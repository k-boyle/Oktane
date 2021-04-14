package kboyle.oktane.reactive.module;

import com.google.common.base.Preconditions;
import kboyle.oktane.reactive.parsers.ReactiveTypeParser;

import java.util.Optional;

/**
 * Represents a parameter of a command.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class ReactiveCommandParameter {
    private final Class<?> type;
    private final Optional<String> description;
    private final String name;
    private final boolean remainder;
    private final ReactiveTypeParser<?> parser;
    private final ReactiveCommand command;

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

    static Builder builder() {
        return new Builder();
    }

    /**
     * @return The underlying type of the parameter.
     */
    public Class<?> type() {
        return type;
    }

    /**
     * @return The parameter's description.
     */
    public Optional<String> description() {
        return description;
    }

    /**
     * @return The parameter's name.
     */
    public String name() {
        return name;
    }

    /**
     * @return Whether or not this parameter is a remainder.
     */
    public boolean remainder() {
        return remainder;
    }

    /**
     * @return The TypeParser used for this parameter.
     */
    public ReactiveTypeParser<?> parser() {
        return parser;
    }

    /**
     * @return The Command that this parameter belongs to.
     */
    public ReactiveCommand command() {
        return command;
    }

    static class Builder {
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
