package kboyle.oktane.reactive.results.precondition;

import kboyle.oktane.reactive.results.FailedResult;

import java.util.List;

public record PreconditionsFailedResult(List<PreconditionResult> failedResults) implements PreconditionResult, FailedResult {
    @Override
    public String reason() {
        return String.format("Failed to execution command due to %d preconditions failing", failedResults.size());
    }
}
