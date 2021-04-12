package kboyle.oktane.reactive.results.argumentparser;

import kboyle.oktane.reactive.module.Command;
import kboyle.oktane.reactive.parsers.ParserFailedReason;
import kboyle.oktane.reactive.results.FailedResult;

public record ArgumentParserFailedResult(Command command, ParserFailedReason failureReason, int index) implements FailedResult, ArgumentParserResult {
    @Override
    public String reason() {
        return failureReason().toString();
    }

    @Override
    public Object[] parsedArguments() {
        return null;
    }
}
