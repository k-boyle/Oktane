package kboyle.oktane.core.results.tokeniser;

import kboyle.oktane.core.results.FailedResult;

import java.util.List;

public record TokeniserTooFewTokensResult(String input, int parameterCount) implements TokeniserResult, FailedResult {
    @Override
    public List<String> tokens() {
        return List.of();
    }

    @Override
    public String reason() {
        return String.format("Failed to tokenise \"%s\" expected %d parameters", input, parameterCount);
    }
}
