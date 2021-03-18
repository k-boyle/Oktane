package kboyle.oktane.example.results;

import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.results.SuccessfulResult;
import kboyle.oktane.core.results.command.CommandResult;

public record KillAppCommandResult(Command command) implements CommandResult, SuccessfulResult {
}
