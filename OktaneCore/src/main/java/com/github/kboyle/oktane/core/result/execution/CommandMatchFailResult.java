package com.github.kboyle.oktane.core.result.execution;

import com.github.kboyle.oktane.core.result.FailResult;
import com.github.kboyle.oktane.core.result.Result;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@ToString
@EqualsAndHashCode
public class CommandMatchFailResult implements FailResult {
    private final List<Result> failureResults;

    public CommandMatchFailResult(List<Result> failureResults) {
        this.failureResults = failureResults;
    }

    @Override
    public String failureReason() {
        return "Failed to find a matching command";
    }

    public List<Result> failureResults() {
        return failureResults;
    }
}
