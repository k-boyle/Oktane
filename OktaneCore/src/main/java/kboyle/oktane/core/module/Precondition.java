package kboyle.oktane.core.module;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.results.preconditions.PreconditionFailedResult;
import kboyle.oktane.core.results.preconditions.PreconditionResult;
import kboyle.oktane.core.results.preconditions.PreconditionSuccessfulResult;

@FunctionalInterface
public interface Precondition {
    PreconditionResult run(CommandContext context);

    default PreconditionSuccessfulResult success() {
        return PreconditionSuccessfulResult.get();
    }

    default PreconditionFailedResult failure(String reason, Object... args) {
        return new PreconditionFailedResult(String.format(reason, args));
    }
}
