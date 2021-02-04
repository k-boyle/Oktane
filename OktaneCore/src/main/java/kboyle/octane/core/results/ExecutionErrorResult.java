package kboyle.octane.core.results;

import kboyle.octane.core.module.Command;
import kboyle.octane.core.results.argumentparser.ArgumentParserResult;

import java.util.Objects;

public record ExecutionErrorResult(Command command, Exception exception) implements ArgumentParserResult, FailedResult {
    @Override
    public String reason() {
        return String.format("An exception was thrown whilst trying to execute %s", command.name());
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExecutionErrorResult that = (ExecutionErrorResult) o;
        return Objects.equals(command, that.command)
            && Objects.equals(exception.getClass(), that.exception.getClass())
            && Objects.equals(exception.getMessage(), that.exception.getMessage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(command, exception);
    }
}
