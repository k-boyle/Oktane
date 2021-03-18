package kboyle.oktane.core.results.preconditions;

import kboyle.oktane.core.results.FailedResult;

public record PreconditionFailedResult(String reason) implements PreconditionResult, FailedResult {
}
