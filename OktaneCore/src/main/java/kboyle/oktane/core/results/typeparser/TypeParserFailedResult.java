package kboyle.oktane.core.results.typeparser;

import kboyle.oktane.core.results.FailedResult;

public record TypeParserFailedResult<T>(String reason) implements TypeParserResult<T>, FailedResult {
    @Override
    public T value() {
        return null;
    }
}
