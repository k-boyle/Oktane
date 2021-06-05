package kboyle.oktane.core.results.search;

import kboyle.oktane.core.results.FailedResult;

public record MissingPrefixResult(String input) implements FailedResult {
    @Override
    public String reason() {
        return String.format("%s does not start with a prefix", input);
    }
}
