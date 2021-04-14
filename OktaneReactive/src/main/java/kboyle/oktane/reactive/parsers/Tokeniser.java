package kboyle.oktane.reactive.parsers;

import kboyle.oktane.reactive.mapping.CommandMatch;
import kboyle.oktane.reactive.results.tokeniser.TokeniserResult;

public interface Tokeniser {
    TokeniserResult tokenise(String input, CommandMatch commandMatch);
}
