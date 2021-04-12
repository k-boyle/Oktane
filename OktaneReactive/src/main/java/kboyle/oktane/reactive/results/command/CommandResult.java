package kboyle.oktane.reactive.results.command;

import kboyle.oktane.reactive.module.Command;
import kboyle.oktane.reactive.results.Result;

public interface CommandResult extends Result {
    Command command();
}
