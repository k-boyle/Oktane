package kboyle.octane.core.results.command;

import kboyle.octane.core.module.Command;
import kboyle.octane.core.results.Result;

public interface CommandResult extends Result {
    Command command();
}
