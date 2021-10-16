package com.github.kboyle.oktane.core.result.execution;

import com.github.kboyle.oktane.core.result.FailResult;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class MissingPrefixResult implements FailResult {
    private final String input;
    private final int startIndex;

    public MissingPrefixResult(String input, int startIndex) {
        this.input = input;
        this.startIndex = startIndex;
    }

    @Override
    public String failureReason() {
        return String.format("%s does not start with a prefix after index %d", input, startIndex);
    }

    public String input() {
        return input;
    }

    public int startIndex() {
        return startIndex;
    }
}
