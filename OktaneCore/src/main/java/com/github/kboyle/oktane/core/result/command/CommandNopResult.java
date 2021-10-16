package com.github.kboyle.oktane.core.result.command;

import com.github.kboyle.oktane.core.command.Command;
import com.github.kboyle.oktane.core.result.SuccessfulResult;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class CommandNopResult implements CommandResult, SuccessfulResult {
    private final Command command;

    public CommandNopResult(Command command) {
        this.command = command;
    }

    @Override
    public Command command() {
        return command;
    }
}
