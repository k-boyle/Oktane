package com.github.kboyle.oktane.core.result.tokeniser;

import com.github.kboyle.oktane.core.result.FailResult;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@ToString
@EqualsAndHashCode
public class TokeniserTooManyTokensResult implements TokeniserResult, FailResult {
    private final String input;
    private final int parameterCount;

    public TokeniserTooManyTokensResult(String input, int parameterCount) {
        this.input = input;
        this.parameterCount = parameterCount;
    }

    @Override
    public List<String> tokens() {
        return List.of();
    }

    @Override
    public String failureReason() {
        return String.format("Failed to tokenise \"%s\" expected %d parameters", input, parameterCount);
    }

    public String input() {
        return input;
    }

    public int parameterCount() {
        return parameterCount;
    }
}
