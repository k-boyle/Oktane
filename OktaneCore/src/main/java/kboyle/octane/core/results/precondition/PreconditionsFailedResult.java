package kboyle.octane.core.results.precondition;

import com.google.common.collect.ImmutableList;
import kboyle.octane.core.results.FailedResult;

public record PreconditionsFailedResult(ImmutableList<FailedResult> results) implements FailedResult, PreconditionResult {
    @Override
    public String reason() {
        return "Precondition checks failed";
    }
}
