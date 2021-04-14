package kboyle.oktane.reactive.results.precondition;

import kboyle.oktane.reactive.results.FailedResult;

public record PreconditionFailedResult(String reason) implements PreconditionResult, FailedResult {
}
