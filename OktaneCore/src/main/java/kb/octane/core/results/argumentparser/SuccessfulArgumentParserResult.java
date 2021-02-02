package kb.octane.core.results.argumentparser;

import kb.octane.core.results.SuccessfulResult;

import java.util.Arrays;

public record SuccessfulArgumentParserResult(Object[] parsedArguments) implements SuccessfulResult, ArgumentParserResult {
    private static class SingletonHolder {
        public static final SuccessfulArgumentParserResult EMPTY = new SuccessfulArgumentParserResult(new Object[0]);
    }

    public static SuccessfulArgumentParserResult empty() {
        return SingletonHolder.EMPTY;
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SuccessfulArgumentParserResult that = (SuccessfulArgumentParserResult) o;
        return Arrays.equals(parsedArguments, that.parsedArguments);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(parsedArguments);
    }
}
