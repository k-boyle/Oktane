package com.github.kboyle.oktane.benchmark;

import com.github.kboyle.oktane.core.execution.CommandContext;

public class BenchmarkContext extends CommandContext {
    private final Object[] arguments;

    public BenchmarkContext(Object[] arguments) {
        this.arguments = arguments;
    }

    @Override
    public Object[] arguments() {
        return arguments;
    }
}
