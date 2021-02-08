package kboyle.oktane.core.results.search;

import com.google.common.collect.ImmutableList;
import kboyle.oktane.core.results.FailedResult;

public record CommandMatchFailedResult(ImmutableList<FailedResult> failedResults) implements FailedResult {
    @Override
    public String reason() {
        return "Failed to find a matching command overload";
    }
}
