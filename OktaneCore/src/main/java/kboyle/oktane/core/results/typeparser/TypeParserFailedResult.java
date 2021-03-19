package kboyle.oktane.core.results.typeparser;

import kboyle.oktane.core.results.FailedResult;

public record TypeParserFailedResult(String reason) implements TypeParserResult, FailedResult {
    @Override
    public Object value() {
        return null;
    }
}
