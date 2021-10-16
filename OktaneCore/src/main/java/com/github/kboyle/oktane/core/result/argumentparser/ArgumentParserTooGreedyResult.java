package com.github.kboyle.oktane.core.result.argumentparser;

import com.github.kboyle.oktane.core.command.CommandParameter;
import com.github.kboyle.oktane.core.result.FailResult;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@ToString
@EqualsAndHashCode
public class ArgumentParserTooGreedyResult<T> implements ArgumentParserResult, FailResult {
    private final CommandParameter<T> parameter;
    private final List<String> tokens;

    public ArgumentParserTooGreedyResult(CommandParameter<T> parameter, List<String> tokens) {
        this.parameter = parameter;
        this.tokens = tokens;
    }

    public CommandParameter<T> parameter() {
        return parameter;
    }

    public List<String> tokens() {
        return tokens;
    }

    @Override
    public String failureReason() {
        return String.format("Argument parsing failed due to %s being too greedy and running out of tokens to parse", parameter);
    }

    @Override
    public Object[] parsedArguments() {
        return new Object[0];
    }
}
