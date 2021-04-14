package kboyle.oktane.reactive.results.tokeniser;

import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.results.FailedResult;

import java.util.List;

public record TokeniserTooFewTokensResult(ReactiveCommand command, String input, int parameterCount) implements TokeniserResult, FailedResult {
    @Override
    public List<String> tokens() {
        return List.of();
    }

    @Override
    public String reason() {
        return String.format("Failed to tokenise \"%s\" expected %d parameters", input, parameterCount);
    }
}
