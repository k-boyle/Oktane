package kboyle.oktane.core.results.precondition;

import kboyle.oktane.core.results.FailedResult;

public record PreconditionFailedResult(String reason) implements PreconditionResult, FailedResult {
}
