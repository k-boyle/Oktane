package kboyle.oktane.reactive.results.command;

import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.results.Result;

public interface CommandResult extends Result {
    ReactiveCommand command();
}
