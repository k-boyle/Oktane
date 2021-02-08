package kboyle.oktane.core.module;

import com.google.common.base.Preconditions;

import java.util.Optional;

/**
 * Represents a parameter of a command.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class CommandParameter {
    private final Class<?> type;
    private final Optional<String> description;
    private final String name;
    private final boolean remainder;

    CommandParameter(
            Class<?> type,
            Optional<String> description,
            String name,
            boolean remainder) {
        this.type = type;
        this.description = description;
        this.name = name;
        this.remainder = remainder;
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

    static class Builder {
        private Class<?> type;
        private String description;
        private String name;
        private boolean remainder;

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

        public CommandParameter build() {
            Preconditions.checkState(type != null, "A parameter type must be specified");
            Preconditions.checkState(name != null, "A parameter name must be specified");
            return new CommandParameter(type, Optional.ofNullable(description), name, remainder);
        }
    }
}
