package com.github.kboyle.oktane.benchmark;

import com.github.kboyle.oktane.core.command.Command;
import com.github.kboyle.oktane.core.execution.CommandContext;

import java.util.List;

public class BenchmarkCommandContext extends CommandContext {
    private final Object[] arguments;
    private final List<String> tokens;
    private final Command command;

    public BenchmarkCommandContext(Object[] arguments) {
        this.arguments = arguments;
        this.command = null;
        this.tokens = null;
    }

    public BenchmarkCommandContext(Command command) {
        this.command = command;
        this.arguments = null;
        this.tokens = null;
    }

    public BenchmarkCommandContext(Command command, List<String> tokens) {
        this.command = command;
        this.tokens = tokens;
        this.arguments = null;
    }

    @Override
    public Object[] arguments() {
        return arguments;
    }

    @Override
    public List<String> tokens() {
        return tokens;
    }

    @Override
    public Command command() {
        return command;
    }
}
