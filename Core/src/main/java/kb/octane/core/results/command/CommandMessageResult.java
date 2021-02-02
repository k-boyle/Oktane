package kb.octane.core.results.command;

import kb.octane.core.module.Command;

public record CommandMessageResult(Command command, String message) implements CommandSuccessfulResult {
    public static CommandMessageResult from(Command command, String message) {
        return new CommandMessageResult(command, message);
    }

    @Override
    public boolean isSuccess() {
        return true;
    }
}
