package kboyle.oktane.reactive.results.argumentparser;

import kboyle.oktane.reactive.results.FailedResult;
import kboyle.oktane.reactive.results.typeparser.TypeParserResult;

public record ArgumentParserFailedToParseArgumentResult(TypeParserResult result) implements FailedResult, ArgumentParserResult {
    @Override
    public String reason() {
        return result instanceof FailedResult failed ? failed.reason() : "";
    }

    @Override
    public Object[] parsedArguments() {
        return null;
    }
}
