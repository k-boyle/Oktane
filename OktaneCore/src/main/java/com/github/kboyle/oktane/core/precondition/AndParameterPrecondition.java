package com.github.kboyle.oktane.core.precondition;

import com.github.kboyle.oktane.core.command.CommandParameter;
import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.result.precondition.ParameterPreconditionResult;
import com.google.common.base.Preconditions;

class AndParameterPrecondition<T> implements ParameterPrecondition<T> {
    private final ParameterPrecondition<T> left;
    private final ParameterPrecondition<T> right;

    AndParameterPrecondition(ParameterPrecondition<T> left, ParameterPrecondition<T> right) {
        this.left = Preconditions.checkNotNull(left, "left cannot be null");
        this.right = Preconditions.checkNotNull(right, "right cannot be null");
    }

    @Override
    public ParameterPreconditionResult<T> run(CommandContext context, CommandParameter<T> parameter, T value) {
        var result = Preconditions.checkNotNull(left.run(context, parameter, value), "run cannot be null");

        if (!result.success()) {
            return result;
        }

        return right.run(context, parameter, value);
    }
}