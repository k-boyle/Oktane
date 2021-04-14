package kboyle.oktane.reactive.results.command;

import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.results.SuccessfulResult;

public record CommandMessageResult(ReactiveCommand command, String message) implements CommandResult, SuccessfulResult {
}
