package kboyle.oktane.reactive.results.argumentparser;

import kboyle.oktane.reactive.results.FailedResult;
import kboyle.oktane.reactive.results.tokeniser.TokeniserResult;

public record ArgumentParserTokenisationFailedResult(TokeniserResult tokeniserResult) implements ArgumentParserResult, FailedResult {
    @Override
    public Object[] parsedArguments() {
        return new Object[0];
    }

    @Override
    public String reason() {
        return String.format("Argument parsing failed due to a tokenisation failure %s", tokeniserResult);
    }
}
