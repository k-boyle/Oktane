package com.github.kboyle.oktane.core.prefix;

import com.github.kboyle.oktane.core.execution.CommandContext;
import com.google.common.base.Preconditions;

public class StringPrefix implements Prefix<String> {
    private final String prefix;

    public StringPrefix(String prefix) {
        this.prefix = Preconditions.checkNotNull(prefix, "prefix cannot be null");
    }

    @Override
    public String value() {
        return prefix;
    }

    @Override
    public int startsWith(CommandContext context, String input, int startIndex) {
        return input.length() > prefix.length() && input.startsWith(prefix, startIndex)
            ? startIndex + prefix.length()
            : -1;
    }
}
