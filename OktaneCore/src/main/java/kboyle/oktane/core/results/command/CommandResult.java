package kboyle.oktane.core.results.command;

import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.results.Result;

public interface CommandResult extends Result {
    Command command();
}
