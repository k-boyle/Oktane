package kboyle.oktane.reactive.results.tokeniser;

import kboyle.oktane.reactive.results.SuccessfulResult;

import java.util.List;

public record TokeniserSuccessfulResult(List<String> tokens) implements TokeniserResult, SuccessfulResult {
    private static class SingletonHolder {
        private static final TokeniserSuccessfulResult INSTANCE = new TokeniserSuccessfulResult(List.of());
    }

    public static TokeniserSuccessfulResult empty() {
        return SingletonHolder.INSTANCE;
    }
}
