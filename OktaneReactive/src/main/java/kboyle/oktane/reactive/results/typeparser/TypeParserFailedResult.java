package kboyle.oktane.reactive.results.typeparser;

import kboyle.oktane.reactive.results.FailedResult;

public record TypeParserFailedResult<T>(String reason) implements TypeParserResult<T>, FailedResult {
    @Override
    public T value() {
        return null;
    }
}
