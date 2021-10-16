package com.github.kboyle.oktane.core.result.precondition;

import com.github.kboyle.oktane.core.result.SuccessfulResult;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class ParameterPreconditionSuccessfulResult<T> implements ParameterPreconditionResult<T>, SuccessfulResult {
    private static final ParameterPreconditionSuccessfulResult<?> INSTANCE = new ParameterPreconditionSuccessfulResult<>();

    @SuppressWarnings("unchecked")
    public static <T> ParameterPreconditionSuccessfulResult<T> get() {
        return (ParameterPreconditionSuccessfulResult<T>) INSTANCE;
    }
}
