package kboyle.oktane.core.results.command;

import kboyle.oktane.core.module.Command;

public record CommandMessageResult(Command command, String message) implements CommandSuccessfulResult {
    public static CommandMessageResult from(Command command, String message) {
        return new CommandMessageResult(command, message);
    }

    @Override
    public boolean isSuccess() {
        return true;
    }
}
