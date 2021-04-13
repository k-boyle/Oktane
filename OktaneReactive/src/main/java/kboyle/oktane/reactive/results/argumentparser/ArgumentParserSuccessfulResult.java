package kboyle.oktane.reactive.results.argumentparser;

import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.results.SuccessfulResult;

import java.util.Arrays;
import java.util.Objects;

public record ArgumentParserSuccessfulResult(ReactiveCommand command, Object[] parsedArguments) implements ArgumentParserResult, SuccessfulResult {
    @Override
    public boolean equals(Object o) {
        return this == o
            || o instanceof ArgumentParserSuccessfulResult that
            && Arrays.equals(parsedArguments, that.parsedArguments);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(command);
        result = 31 * result + Arrays.hashCode(parsedArguments);
        return result;
    }

    @Override
    public String toString() {
        return "ArgumentParserSuccessfulResult{" +
            "command=" + command +
            ", parsedArguments=" + Arrays.toString(parsedArguments) +
            '}';
    }
}
