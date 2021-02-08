package kboyle.oktane.core.results.precondition;

import kboyle.oktane.core.results.SuccessfulResult;

public class SuccessfulPreconditionResult implements SuccessfulResult, PreconditionResult {
    private static class SingletonHolder {
        private static final SuccessfulPreconditionResult INSTANCE = new SuccessfulPreconditionResult();
    }

    public static SuccessfulPreconditionResult get() {
        return SingletonHolder.INSTANCE;
    }

    private SuccessfulPreconditionResult() {
    }
}
