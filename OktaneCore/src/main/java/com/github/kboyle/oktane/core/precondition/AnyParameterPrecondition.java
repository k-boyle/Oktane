package com.github.kboyle.oktane.core.precondition;

import com.github.kboyle.oktane.core.command.CommandParameter;
import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.result.precondition.ParameterPreconditionResult;
import com.github.kboyle.oktane.core.result.precondition.ParameterPreconditionsFailResult;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

class AnyParameterPrecondition<T> implements ParameterPrecondition<T> {
    private final List<ParameterPrecondition<T>> preconditions;

    AnyParameterPrecondition(List<ParameterPrecondition<T>> preconditions) {
        for (var precondition : preconditions) {
            Preconditions.checkNotNull(precondition, "precondition cannot be null");
        }

        this.preconditions = preconditions;
    }

    @Override
    public ParameterPreconditionResult<T> run(CommandContext context, CommandParameter<T> parameter, T value) {
        var failures = new ArrayList<ParameterPreconditionResult<T>>();
        for (var precondition : preconditions) {
            var result = Preconditions.checkNotNull(
                precondition.run(context, parameter, value),
                "ParameterPrecondition#run cannot return null"
            );

            if (result.success()) {
                return result;
            }

            failures.add(result);
        }

        return new ParameterPreconditionsFailResult<>(failures);
    }
}