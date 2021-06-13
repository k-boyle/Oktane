package kboyle.oktane.core.results.precondition;

import kboyle.oktane.core.module.CommandParameter;
import kboyle.oktane.core.results.FailedResult;

public record ParameterPreconditionsFailedResult(CommandParameter parameter, Object argument, PreconditionResult result) implements PreconditionResult, FailedResult {
    @Override
    public String reason() {
        return String.format("Failed to execution command due to %s preconditions failing", parameter);
    }
}
