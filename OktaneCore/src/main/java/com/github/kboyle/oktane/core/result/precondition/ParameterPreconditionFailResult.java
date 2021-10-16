package com.github.kboyle.oktane.core.result.precondition;

import com.github.kboyle.oktane.core.command.CommandParameter;
import com.github.kboyle.oktane.core.result.FailResult;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class ParameterPreconditionFailResult<T> implements ParameterPreconditionResult<T>, FailResult {
    private final CommandParameter<T> parameter;
    private final T value;
    private final String failureReason;

    public ParameterPreconditionFailResult(CommandParameter<T> parameter, T value, String failureReason) {
        this.parameter = parameter;
        this.value = value;
        this.failureReason = failureReason;
    }

    public CommandParameter<T> parameter() {
        return parameter;
    }

    public T value() {
        return value;
    }

    public String failureReason() {
        return failureReason;
    }
}
