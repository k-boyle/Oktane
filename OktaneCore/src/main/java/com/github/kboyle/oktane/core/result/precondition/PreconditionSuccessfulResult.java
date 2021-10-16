package com.github.kboyle.oktane.core.result.precondition;

import com.github.kboyle.oktane.core.result.SuccessfulResult;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public final class PreconditionSuccessfulResult implements PreconditionResult, SuccessfulResult {
    private static final PreconditionSuccessfulResult INSTANCE = new PreconditionSuccessfulResult();

    public PreconditionSuccessfulResult() {
    }

    public static PreconditionSuccessfulResult get() {
        return INSTANCE;
    }
}
