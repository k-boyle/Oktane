package kboyle.oktane.core.results.argumentparser;

import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.parsers.ParserFailedReason;
import kboyle.oktane.core.results.FailedResult;

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
