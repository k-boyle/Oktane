package kboyle.oktane.reactive.results.tokeniser;

import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.results.FailedResult;

import java.util.List;

public record TokeniserMissingQuoteResult(ReactiveCommand command, String input, int index) implements TokeniserResult, FailedResult {
    @Override
    public List<String> tokens() {
        return List.of();
    }

    @Override
    public String reason() {
        return String.format("Expected quote at index %d", index);
    }
}
