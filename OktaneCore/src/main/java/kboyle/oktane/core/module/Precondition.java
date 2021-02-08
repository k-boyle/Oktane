package kboyle.oktane.core.module;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.results.precondition.FailurePreconditionResult;
import kboyle.oktane.core.results.precondition.PreconditionResult;
import kboyle.oktane.core.results.precondition.SuccessfulPreconditionResult;

@FunctionalInterface
public interface Precondition {
    PreconditionResult run(CommandContext context);

    default SuccessfulPreconditionResult success() {
        return SuccessfulPreconditionResult.get();
    }

    default FailurePreconditionResult failure(String reason, Object... args) {
        return new FailurePreconditionResult(String.format(reason, args));
    }
}
