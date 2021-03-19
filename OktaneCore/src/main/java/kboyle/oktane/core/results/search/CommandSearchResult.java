package kboyle.oktane.core.results.search;

import kboyle.oktane.core.results.FailedResult;

public interface CommandSearchResult extends FailedResult {
    String reason();
}
