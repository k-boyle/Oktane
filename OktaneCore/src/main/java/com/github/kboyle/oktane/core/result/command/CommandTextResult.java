package com.github.kboyle.oktane.core.result.command;

import com.github.kboyle.oktane.core.command.Command;
import com.github.kboyle.oktane.core.result.SuccessfulResult;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class CommandTextResult implements CommandResult, SuccessfulResult {
    private final Command command;
    private final String text;

    public CommandTextResult(Command command, String text) {
        this.command = command;
        this.text = text;
    }

    @Override
    public Command command() {
        return command;
    }

    public String text() {
        return text;
    }
}
