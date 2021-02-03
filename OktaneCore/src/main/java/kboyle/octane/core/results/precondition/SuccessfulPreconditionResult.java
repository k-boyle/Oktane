package kboyle.octane.core.results.precondition;

import kboyle.octane.core.results.SuccessfulResult;

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
