package kboyle.octane.core.module;

import kboyle.octane.core.CommandContext;
import kboyle.octane.core.results.precondition.FailurePreconditionResult;
import kboyle.octane.core.results.precondition.PreconditionResult;
import kboyle.octane.core.results.precondition.SuccessfulPreconditionResult;

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
