package kboyle.octane.core.results.command;

import kboyle.octane.core.module.Command;

public record CommandMessageResult(Command command, String message) implements CommandSuccessfulResult {
    public static CommandMessageResult from(Command command, String message) {
        return new CommandMessageResult(command, message);
    }

    @Override
    public boolean isSuccess() {
        return true;
    }
}
