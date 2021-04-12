package kboyle.oktane.reactive.results.execution;

import kboyle.oktane.reactive.ExecutionStep;
import kboyle.oktane.reactive.module.Command;
import kboyle.oktane.reactive.results.FailedResult;

public record ExecutionExceptionResult(Command command, Exception exception, ExecutionStep step) implements FailedResult {
    public String reason() {
        return String.format("An exception was thrown during step %s", step);
    }
}
