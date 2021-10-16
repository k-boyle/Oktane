package com.github.kboyle.oktane.core.precondition;

import com.github.kboyle.oktane.core.command.Command;
import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.result.precondition.PreconditionResult;
import com.google.common.base.Preconditions;

class OrPrecondition implements Precondition {
    private final Precondition left;
    private final Precondition right;

    OrPrecondition(Precondition left, Precondition right) {
        this.left = Preconditions.checkNotNull(left, "left cannot be null");;
        this.right = Preconditions.checkNotNull(right, "right cannot be null");;
    }

    @Override
    public PreconditionResult run(CommandContext context, Command command) {
        var result = Preconditions.checkNotNull(left.run(context, command), "run cannot be null");

        if (result.success()) {
            return result;
        }

        return right.run(context, command);
    }
}
