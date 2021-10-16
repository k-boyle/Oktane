package com.github.kboyle.oktane.core.parsing;

import com.github.kboyle.oktane.core.mapping.CommandMatch;
import com.github.kboyle.oktane.core.result.tokeniser.TokeniserResult;

public interface Tokeniser {
    TokeniserResult tokenise(String input, CommandMatch commandMatch);

    static Tokeniser get() {
        return new DefaultTokeniser();
    }
}
