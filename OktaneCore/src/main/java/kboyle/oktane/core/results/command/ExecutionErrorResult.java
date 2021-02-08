package kboyle.oktane.core.results.command;

import kboyle.oktane.core.module.Command;

public record ExecutionErrorResult(Command command, Exception exception) implements CommandFailedResult {
    @Override
    public String reason() {
        return String.format("An exception was thrown whilst trying to execute %s", command.name());
    }
}
