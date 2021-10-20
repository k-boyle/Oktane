package com.github.kboyle.oktane.core.result.tokeniser;

import com.github.kboyle.oktane.core.result.SuccessfulResult;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@ToString
@EqualsAndHashCode
public class TokeniserSuccessfulResult implements TokeniserResult, SuccessfulResult {
    private static final TokeniserSuccessfulResult INSTANCE = new TokeniserSuccessfulResult(List.of());
    private final List<String> tokens;

    public TokeniserSuccessfulResult(List<String> tokens) {
        this.tokens = tokens;
    }

    public static TokeniserSuccessfulResult empty() {
        return INSTANCE;
    }

    public List<String> tokens() {
        return tokens;
    }
}
