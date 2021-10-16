package com.github.kboyle.oktane.core.command;

import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.precondition.Precondition;
import com.github.kboyle.oktane.core.result.precondition.*;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

public enum CommandUtil {
    ;

    public static PreconditionResult runPreconditions(List<Precondition> preconditions, CommandContext context, Command command) {
        Preconditions.checkNotNull(preconditions, "preconditions cannot be null");
        Preconditions.checkNotNull(context, "context cannot be null");
        Preconditions.checkNotNull(command, "command cannot be null");

        var failures = new ArrayList<PreconditionResult>();
        for (var precondition : preconditions) {
            var result = precondition.run(context, command);
            if (!result.success()) {
                failures.add(result);
            }
        }

        return failures.isEmpty()
            ? PreconditionSuccessfulResult.get()
            : new PreconditionsFailResult(failures);
    }
}
