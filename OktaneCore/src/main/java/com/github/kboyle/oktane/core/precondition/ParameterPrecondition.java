package com.github.kboyle.oktane.core.precondition;

import com.github.kboyle.oktane.core.command.CommandParameter;
import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.result.precondition.*;
import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.Optional;

public interface ParameterPrecondition<T> {
    ParameterPreconditionResult<T> run(CommandContext context, CommandParameter<T> parameter, T value);

    default ParameterPreconditionResult<T> success() {
        return ParameterPreconditionSuccessfulResult.get();
    }

    default ParameterPreconditionResult<T> failure(CommandParameter<T> parameter, T value, String reason, Object... args) {
        return new ParameterPreconditionFailResult<>(parameter, value, String.format(reason, args));
    }

    default Optional<Object> group() {
        return Optional.empty();
    }

    default ParameterPrecondition<T> or(ParameterPrecondition<T> or) {
        return new OrParameterPrecondition<>(this, or);
    }

    default ParameterPrecondition<T> and(ParameterPrecondition<T> and) {
        return new AndParameterPrecondition<>(this, and);
    }

    @SafeVarargs
    static <T> ParameterPrecondition<T> any(
            ParameterPrecondition<T> first,
            ParameterPrecondition<T> second,
            ParameterPrecondition<T> third,
            ParameterPrecondition<T>... rest) {

        Preconditions.checkNotNull(first, "first cannot be null");
        Preconditions.checkNotNull(second, "second cannot be null");
        Preconditions.checkNotNull(third, "third cannot be null");
        Preconditions.checkNotNull(rest, "rest cannot be null");

        var preconditions = Arrays.asList(first, second, third);
        preconditions.addAll(Arrays.asList(rest));

        return new AnyParameterPrecondition<>(preconditions);
    }
}
