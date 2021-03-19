package kboyle.oktane.core.results.precondition;

import com.google.common.collect.ImmutableList;
import kboyle.oktane.core.results.FailedResult;

public record PreconditionsFailedResult(ImmutableList<PreconditionResult> failedResults) implements PreconditionResult, FailedResult {
    @Override
    public String reason() {
        return String.format("Failed to execution command due to %d preconditions failing", failedResults.size());
    }
}
