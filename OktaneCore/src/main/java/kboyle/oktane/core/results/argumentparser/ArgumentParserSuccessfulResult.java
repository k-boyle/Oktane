package kboyle.oktane.core.results.argumentparser;

import kboyle.oktane.core.results.SuccessfulResult;

import java.util.Arrays;

public record ArgumentParserSuccessfulResult(Object[] parsedArguments) implements ArgumentParserResult, SuccessfulResult {
    private static class SingletonHolder {
        public static final ArgumentParserSuccessfulResult EMPTY = new ArgumentParserSuccessfulResult(new Object[0]);
    }

    public static ArgumentParserSuccessfulResult empty() {
        return SingletonHolder.EMPTY;
    }

    @Override
    public boolean equals(Object o) {
        return this == o
            || o instanceof ArgumentParserSuccessfulResult that
            && Arrays.equals(parsedArguments, that.parsedArguments);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(parsedArguments);
    }
}