package com.github.kboyle.oktane.core.prefix;

import com.github.kboyle.oktane.core.execution.CommandContext;

public interface Prefix<T> {
    T value();
    int startsWith(CommandContext context, String input, int startIndex);

    default int startsWith(CommandContext context, String input) {
        return startsWith(context, input, 0);
    }
}
