package kboyle.octane.core.results.precondition;

import kboyle.octane.core.results.FailedResult;

public record FailurePreconditionResult(String reason) implements FailedResult, PreconditionResult {
}
