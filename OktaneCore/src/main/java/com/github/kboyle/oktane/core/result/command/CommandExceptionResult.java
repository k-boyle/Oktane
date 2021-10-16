package com.github.kboyle.oktane.core.result.command;

import com.github.kboyle.oktane.core.command.Command;
import com.github.kboyle.oktane.core.result.ExceptionResult;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class CommandExceptionResult implements CommandResult, ExceptionResult {
    private final Command command;
    private final Exception exception;

    public CommandExceptionResult(Command command, Exception exception) {
        this.command = command;
        this.exception = exception;
    }

    @Override
    public String failureReason() {
        return String.format("An exception was thrown whilst trying to execute %s", command);
    }

    @Override
    public Command command() {
        return command;
    }

    @Override
    public Exception exception() {
        return exception;
    }
}
