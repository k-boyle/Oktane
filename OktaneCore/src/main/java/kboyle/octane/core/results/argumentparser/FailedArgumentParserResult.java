package kboyle.octane.core.results.argumentparser;

import kboyle.octane.core.module.Command;
import kboyle.octane.core.results.FailedResult;

public record FailedArgumentParserResult(Command command, Reason failureReason, int index) implements ArgumentParserResult, FailedResult {
    public enum Reason {
        TOO_FEW_ARGUMENTS,
        TOO_MANY_ARGUMENTS,
        MISSING_QUOTE,
        ;
    }

    @Override
    public String reason() {
        return failureReason().toString();
    }
}
