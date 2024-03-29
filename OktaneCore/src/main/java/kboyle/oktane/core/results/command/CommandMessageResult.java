package kboyle.oktane.core.results.command;

import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.results.SuccessfulResult;

public record CommandMessageResult(Command command, String message) implements CommandResult, SuccessfulResult {
}
