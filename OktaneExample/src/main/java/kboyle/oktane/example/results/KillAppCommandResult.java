package kboyle.oktane.example.results;

import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.results.command.CommandSuccessfulResult;

public record KillAppCommandResult(Command command) implements CommandSuccessfulResult {
}
