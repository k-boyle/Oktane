package com.github.kboyle.oktane.core.result.tokeniser;

import com.github.kboyle.oktane.core.result.FailResult;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@ToString
@EqualsAndHashCode
public class TokeniserMissingQuoteResult implements TokeniserResult, FailResult {
    private final String input;
    private final int index;

    public TokeniserMissingQuoteResult(String input, int index) {
        this.input = input;
        this.index = index;
    }

    @Override
    public List<String> tokens() {
        return List.of();
    }

    @Override
    public String failureReason() {
        return String.format("Expected quote at index %d", index);
    }

    public String input() {
        return input;
    }

    public int index() {
        return index;
    }
}
