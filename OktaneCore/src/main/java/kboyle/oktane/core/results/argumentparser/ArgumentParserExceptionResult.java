package kboyle.oktane.core.results.argumentparser;

import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.results.ExceptionResult;
import kboyle.oktane.core.results.FailedResult;

import java.util.Objects;

public record ArgumentParserExceptionResult(Command command, Exception exception) implements FailedResult, ExceptionResult, ArgumentParserResult {
    @Override
    public String reason() {
        return String.format("An exception was thrown whilst trying to execute %s", command.name());
    }

    @Override
    public Object[] parsedArguments() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        return this == o
            || o instanceof ArgumentParserExceptionResult that
            && Objects.equals(command, that.command)
            && Objects.equals(exception.getClass(), that.exception.getClass())
            && Objects.equals(exception.getMessage(), that.exception.getMessage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(command, exception);
    }
}