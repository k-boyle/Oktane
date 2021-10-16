package com.github.kboyle.oktane.core.result.argumentparser;

import com.github.kboyle.oktane.core.result.SuccessfulResult;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class ArgumentParserSuccessfulResult implements ArgumentParserResult, SuccessfulResult {
    private static final ArgumentParserSuccessfulResult EMPTY = new ArgumentParserSuccessfulResult(new Object[0]);

    private final Object[] parsedArguments;

    public ArgumentParserSuccessfulResult(Object[] parsedArguments) {
        this.parsedArguments = parsedArguments;
    }

    public static ArgumentParserSuccessfulResult empty() {
        return EMPTY;
    }

    @Override
    public Object[] parsedArguments() {
        return parsedArguments;
    }
}
