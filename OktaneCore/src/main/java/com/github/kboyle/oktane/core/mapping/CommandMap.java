package com.github.kboyle.oktane.core.mapping;

import java.util.List;

public interface CommandMap {
    List<CommandMatch> findMatches(String input, int startIndex);

    static CommandMapProvider provider() {
        return ImmutableCommandMapNode::create;
    }
}
