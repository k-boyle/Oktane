package com.github.kboyle.oktane.core.result.command;

import com.github.kboyle.oktane.core.command.Command;
import com.github.kboyle.oktane.core.result.Result;

public interface CommandResult extends Result {
    Command command();
}
