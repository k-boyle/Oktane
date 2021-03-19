package kboyle.oktane.core.results.execution;

import kboyle.oktane.core.ExecutionStep;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.results.FailedResult;

public record ExecutionExceptionResult(Command command, Exception exception, ExecutionStep step) implements FailedResult {
    public String reason() {
        return String.format("An exception was thrown during step %s", step);
    }
}
