package kboyle.oktane.core.results.argumentparser;

import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.results.FailedResult;
import kboyle.oktane.core.results.Result;

import java.util.List;

public record ArgumentParserFailedResult(Command command, List<Result> results) implements FailedResult, ArgumentParserResult {
    @Override
    public String reason() {
        return String.format("Argument parsing failed for command %s", command.name);
    }

    @Override
    public Object[] parsedArguments() {
        return null;
    }
}
