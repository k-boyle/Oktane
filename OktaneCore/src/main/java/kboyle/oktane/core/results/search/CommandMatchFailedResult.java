package kboyle.oktane.core.results.search;

import com.google.common.collect.ImmutableList;
import kboyle.oktane.core.results.Result;

public record CommandMatchFailedResult(ImmutableList<Result> failedResults) implements CommandSearchResult {
    @Override
    public String reason() {
        return "Failed to find a matching command overload";
    }
}
