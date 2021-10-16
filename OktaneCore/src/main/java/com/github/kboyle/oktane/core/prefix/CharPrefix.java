package com.github.kboyle.oktane.core.prefix;

import com.github.kboyle.oktane.core.execution.CommandContext;

public class CharPrefix implements Prefix<Character> {
    private final char prefix;

    public CharPrefix(char prefix) {
        this.prefix = prefix;
    }

    @Override
    public Character value() {
        return prefix;
    }

    @Override
    public int startsWith(CommandContext context, String input, int startIndex) {
        return startIndex == 0 && input.length() > 1 && input.charAt(0) == prefix
            ? 1
            : -1;
    }
}
