package kboyle.oktane.reactive.results.argumentparser;

import kboyle.oktane.reactive.results.Result;

public interface ArgumentParserResult extends Result {
    Object[] parsedArguments();
}
