package kboyle.oktane.reactive.results.command;

import kboyle.oktane.reactive.module.Command;
import kboyle.oktane.reactive.results.SuccessfulResult;

public record CommandNOPResult(Command command) implements CommandResult, SuccessfulResult {
}
