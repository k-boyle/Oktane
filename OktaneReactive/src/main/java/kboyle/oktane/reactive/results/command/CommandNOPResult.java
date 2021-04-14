package kboyle.oktane.reactive.results.command;

import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.results.SuccessfulResult;

public record CommandNOPResult(ReactiveCommand command) implements CommandResult, SuccessfulResult {
}
