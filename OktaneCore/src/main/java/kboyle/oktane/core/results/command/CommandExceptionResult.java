package kboyle.oktane.core.results.command;

import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.results.ExceptionResult;

public record CommandExceptionResult(Command command, Exception exception) implements ExceptionResult, CommandResult {
    @Override
    public String reason() {
        return String.format("An exception was thrown whilst trying to execute %s", command.name);
    }
}
