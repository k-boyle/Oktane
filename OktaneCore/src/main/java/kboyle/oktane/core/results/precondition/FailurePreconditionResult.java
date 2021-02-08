package kboyle.oktane.core.results.precondition;

import kboyle.oktane.core.results.FailedResult;

public record FailurePreconditionResult(String reason) implements FailedResult, PreconditionResult {
}
