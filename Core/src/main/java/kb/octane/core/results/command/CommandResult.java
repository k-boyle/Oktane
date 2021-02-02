package kb.octane.core.results.command;

import kb.octane.core.module.Command;
import kb.octane.core.results.Result;

public interface CommandResult extends Result {
    Command command();
}
