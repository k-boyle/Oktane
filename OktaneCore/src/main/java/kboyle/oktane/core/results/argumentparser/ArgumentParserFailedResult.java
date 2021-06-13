package kboyle.oktane.core.results.argumentparser;

import kboyle.oktane.core.module.CommandParameter;
import kboyle.oktane.core.results.FailedResult;
import kboyle.oktane.core.results.typeparser.TypeParserResult;

public record ArgumentParserFailedResult(CommandParameter parameter, String token, TypeParserResult<?> result) implements FailedResult, ArgumentParserResult {
    @Override
    public String reason() {
        return String.format("Argument parsing failed due to not being able to parse %s as %s", token, parameter.type);
    }

    @Override
    public Object[] parsedArguments() {
        return null;
    }
}
