package com.github.kboyle.oktane.core.command;

import com.github.kboyle.oktane.core.precondition.Precondition;
import com.google.common.base.Preconditions;
import lombok.ToString;

import java.util.List;
import java.util.Objects;

@ToString
public class DefaultCommandSignature implements CommandSignature {
    private final List<CommandParameter<?>> parameters;
    private final List<Precondition> preconditions;

    protected DefaultCommandSignature(List<CommandParameter<?>> parameters, List<Precondition> preconditions) {
        this.parameters = parameters;
        this.preconditions = preconditions;
    }

    @Override
    public boolean equals(Object o) {
        return this == o
            || o instanceof DefaultCommandSignature other
            && equalParameters(other.parameters)
            && equalPreconditions(other.preconditions);
    }

    private boolean equalParameters(List<CommandParameter<?>> other) {
        Preconditions.checkNotNull(other, "other cannot be null");

        if (parameters.size() != other.size()) {
            return false;
        }

        for (int i = 0; i < parameters.size(); i++) {
            var left = parameters.get(i);
            var right = other.get(i);

            if (left.type() != right.type() || left.remainder() && right.remainder()) {
                return false;
            }
        }

        return true;
    }

    private boolean equalPreconditions(List<Precondition> other) {
        Preconditions.checkNotNull(other, "other cannot be null");

        if (preconditions.size() != other.size()) {
            return false;
        }

        return preconditions.equals(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameters, preconditions);
    }
}
