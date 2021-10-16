package com.github.kboyle.oktane.core.result.precondition;

import com.github.kboyle.oktane.core.result.FailResult;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@ToString
@EqualsAndHashCode
public class PreconditionsFailResult implements PreconditionResult, FailResult {
    private final List<PreconditionResult> failedResults;

    public PreconditionsFailResult(List<PreconditionResult> failedResults) {
        this.failedResults = failedResults;
    }

    @Override
    public String failureReason() {
        return String.format("Failed due to %d preconditions failing to pass", failedResults.size());
    }

    public List<PreconditionResult> failedResults() {
        return failedResults;
    }
}
