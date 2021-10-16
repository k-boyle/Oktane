package com.github.kboyle.oktane.core.precondition;

import com.github.kboyle.oktane.core.command.Command;
import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.result.precondition.PreconditionResult;
import com.github.kboyle.oktane.core.result.precondition.PreconditionsFailResult;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

class AnyPrecondition implements Precondition {
    private final List<Precondition> preconditions;

    AnyPrecondition(List<Precondition> preconditions) {
        for (var precondition : preconditions) {
            Preconditions.checkNotNull(precondition, "precondition cannot be null");
        }

        this.preconditions = preconditions;
    }

    @Override
    public PreconditionResult run(CommandContext context, Command command) {
        var failures = new ArrayList<PreconditionResult>();
        for (var precondition : preconditions) {
            var result = precondition.run(context, command);
            if (result.success()) {
                return result;
            }

            failures.add(result);
        }

        return new PreconditionsFailResult(failures);
    }
}
