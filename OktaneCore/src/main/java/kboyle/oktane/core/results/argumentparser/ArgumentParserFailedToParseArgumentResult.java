package kboyle.oktane.core.results.argumentparser;

import kboyle.oktane.core.results.FailedResult;
import kboyle.oktane.core.results.typeparser.TypeParserResult;

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
