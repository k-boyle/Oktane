package kb.octane.core.results.command;

import kb.octane.core.module.Command;

public record CommandExceptionResult(Command command, Exception exception) implements CommandFailedResult {
    @Override
    public String reason() {
        return String.format("An exception was thrown whilst trying to execute %s", command.name());
    }
}
