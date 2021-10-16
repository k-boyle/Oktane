package com.github.kboyle.oktane.core.execution;

import com.github.kboyle.oktane.core.result.command.CommandResult;

@FunctionalInterface
public interface CommandCallback {
    CommandResult execute(CommandContext context);
}
