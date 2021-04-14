package kboyle.oktane.reactive.results.search;

import kboyle.oktane.reactive.results.Result;

import java.util.List;

public record CommandMatchFailedResult(List<Result> failedResults) implements CommandSearchResult {
    @Override
    public String reason() {
        return "Failed to find a matching command overload";
    }
}
