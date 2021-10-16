package com.github.kboyle.oktane.core.result.precondition;

import com.github.kboyle.oktane.core.result.FailResult;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@ToString
@EqualsAndHashCode
public class ParameterPreconditionsFailResult<T> implements ParameterPreconditionResult<T>, FailResult {
    private final List<ParameterPreconditionResult<T>> failedResults;

    public ParameterPreconditionsFailResult(List<ParameterPreconditionResult<T>> failedResults) {
        this.failedResults = failedResults;
    }

    @Override
    public String failureReason() {
        return String.format("Failed due to %d preconditions failing to pass", failedResults.size());
    }

    public List<ParameterPreconditionResult<T>> failedResults() {
        return failedResults;
    }
}
