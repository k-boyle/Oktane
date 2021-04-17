package kboyle.oktane.reactive.results.argumentparser;

import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.results.FailedResult;
import kboyle.oktane.reactive.results.Result;

import java.util.List;

public record ArgumentParserFailedResult(ReactiveCommand command, List<Result> results) implements FailedResult, ArgumentParserResult {
    @Override
    public String reason() {
        return String.format("Argument parsing failed for command %s", command.name);
    }

    @Override
    public Object[] parsedArguments() {
        return null;
    }
}
