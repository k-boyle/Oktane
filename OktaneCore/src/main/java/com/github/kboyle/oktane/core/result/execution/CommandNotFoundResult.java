package com.github.kboyle.oktane.core.result.execution;

import com.github.kboyle.oktane.core.result.FailResult;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class CommandNotFoundResult implements FailResult {
    private static final CommandNotFoundResult INSTANCE = new CommandNotFoundResult();

    @Override
    public String failureReason() {
        return "No command was found";
    }

    public static CommandNotFoundResult get() {
        return INSTANCE;
    }
}
