package com.github.kboyle.oktane.core;

import com.github.kboyle.oktane.core.command.Command;
import com.github.kboyle.oktane.core.execution.CommandContext;

import java.util.List;

public class TestCommandContext extends CommandContext {
    private final Command command;
    private final List<String> tokens;

    public TestCommandContext(Command command) {
        this.command = command;
        this.tokens = null;
    }

    public TestCommandContext(Command command, List<String> tokens) {
        this.command = command;
        this.tokens = tokens;
    }

    @Override
    public Command command() {
        return command;
    }

    @Override
    public List<String> tokens() {
        return tokens;
    }
}
