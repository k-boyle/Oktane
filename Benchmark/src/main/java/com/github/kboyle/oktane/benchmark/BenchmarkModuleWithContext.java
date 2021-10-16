package com.github.kboyle.oktane.benchmark;

import com.github.kboyle.oktane.core.annotation.Aliases;
import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.execution.ModuleBase;
import com.github.kboyle.oktane.core.result.command.CommandResult;

public class BenchmarkModuleWithContext extends ModuleBase<CommandContext> {
    private final CommandContext context;

    public BenchmarkModuleWithContext(CommandContext context) {
        this.context = context;
    }

    @Aliases("none")
    public CommandResult none() {
        return nop();
    }

    @Aliases("one")
    public CommandResult one(String a) {
        return nop();
    }

    @Aliases("two")
    public CommandResult two(String a, String b) {
        return nop();
    }

    @Override
    public CommandContext context() {
        return context;
    }
}
