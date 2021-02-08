package kboyle.oktane.core.results.command;

import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.results.SuccessfulResult;

public interface CommandSuccessfulResult extends CommandResult, SuccessfulResult {
    record NOP(Command command) implements CommandSuccessfulResult {
    }
}
