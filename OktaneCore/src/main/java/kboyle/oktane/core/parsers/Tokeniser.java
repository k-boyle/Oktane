package kboyle.oktane.core.parsers;

import kboyle.oktane.core.mapping.CommandMatch;
import kboyle.oktane.core.results.tokeniser.TokeniserResult;

public interface Tokeniser {
    TokeniserResult tokenise(String input, CommandMatch commandMatch);
}
