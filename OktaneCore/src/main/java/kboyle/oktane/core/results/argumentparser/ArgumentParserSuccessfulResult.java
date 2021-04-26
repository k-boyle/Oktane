package kboyle.oktane.core.results.argumentparser;

import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.results.SuccessfulResult;

import java.util.Arrays;
import java.util.Objects;

public record ArgumentParserSuccessfulResult(Command command, Object[] parsedArguments) implements ArgumentParserResult, SuccessfulResult {
    @Override
    public boolean equals(Object o) {
        return this == o
            || o instanceof ArgumentParserSuccessfulResult that
            && Arrays.equals(parsedArguments, that.parsedArguments);
    }

    @Override
    public int hashCode() {
        var result = Objects.hash(command);
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
