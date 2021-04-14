package kboyle.oktane.reactive.results.precondition;

import kboyle.oktane.reactive.results.SuccessfulResult;

public record PreconditionSuccessfulResult() implements PreconditionResult, SuccessfulResult {
    private static class SingletonHolder {
        public static final PreconditionSuccessfulResult INSTANCE = new PreconditionSuccessfulResult();
    }

    public static PreconditionSuccessfulResult get() {
        return SingletonHolder.INSTANCE;
    }
}
