package kb.octane.core.results.precondition;

import kb.octane.core.results.FailedResult;

public record FailurePreconditionResult(String reason) implements FailedResult, PreconditionResult {
}
