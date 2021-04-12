package kboyle.oktane.reactive.results.search;

import kboyle.oktane.reactive.results.FailedResult;

public interface CommandSearchResult extends FailedResult {
    String reason();
}
