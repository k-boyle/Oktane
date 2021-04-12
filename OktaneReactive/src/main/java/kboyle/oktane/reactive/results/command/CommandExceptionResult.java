package kboyle.oktane.reactive.results.command;

import kboyle.oktane.reactive.module.Command;
import kboyle.oktane.reactive.results.ExceptionResult;

public record CommandExceptionResult(Command command, Exception exception) implements ExceptionResult, CommandResult {
    @Override
    public String reason() {
        return String.format("An exception was thrown whilst trying to execute %s", command.name());
    }
}
