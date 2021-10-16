package com.github.kboyle.oktane.core.result.precondition;

import com.github.kboyle.oktane.core.precondition.Precondition;
import com.github.kboyle.oktane.core.result.FailResult;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class PreconditionFailResult implements PreconditionResult, FailResult {
    private final Precondition precondition;
    private final String failureReason;

    public PreconditionFailResult(Precondition precondition, String failureReason) {
        this.precondition = precondition;
        this.failureReason = failureReason;
    }

    public Precondition precondition() {
        return precondition;
    }

    public String failureReason() {
        return failureReason;
    }
}
