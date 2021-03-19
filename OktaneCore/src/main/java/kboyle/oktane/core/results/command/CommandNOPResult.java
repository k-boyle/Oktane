package kboyle.oktane.core.results.command;

import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.results.SuccessfulResult;

public record CommandNOPResult(Command command) implements CommandResult, SuccessfulResult {
}
