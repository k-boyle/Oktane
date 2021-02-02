package kb.octane.core.module;

import kb.octane.core.CommandContext;
import kb.octane.core.results.precondition.FailurePreconditionResult;
import kb.octane.core.results.precondition.PreconditionResult;
import kb.octane.core.results.precondition.SuccessfulPreconditionResult;

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
