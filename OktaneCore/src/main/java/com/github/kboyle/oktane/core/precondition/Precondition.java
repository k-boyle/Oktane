package com.github.kboyle.oktane.core.precondition;

import com.github.kboyle.oktane.core.command.Command;
import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.result.precondition.*;
import com.google.common.base.Preconditions;

import java.util.*;

@FunctionalInterface
public interface Precondition {
    PreconditionResult run(CommandContext context, Command command);

    default PreconditionResult success() {
        return PreconditionSuccessfulResult.get();
    }

    default PreconditionResult failure(String reason, Object... args) {
        return new PreconditionFailResult(this, String.format(reason, args));
    }

    default Optional<Object> group() {
        return Optional.empty();
    }

    default Precondition or(Precondition or) {
        return new OrPrecondition(this, or);
    }

    default Precondition and(Precondition and) {
        return new AndPrecondition(this, and);
    }

    static Precondition any(Precondition first, Precondition second, Precondition third, Precondition... rest) {
        Preconditions.checkNotNull(first, "first cannot be null");
        Preconditions.checkNotNull(second, "second cannot be null");
        Preconditions.checkNotNull(third, "third cannot be null");
        Preconditions.checkNotNull(rest, "rest cannot be null");

        var preconditions = Arrays.asList(first, second, third);
        Collections.addAll(Arrays.asList(rest));

        return new AnyPrecondition(preconditions);
    }
}
