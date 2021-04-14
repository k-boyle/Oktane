package kboyle.oktane.reactive.results.typeparser;

import kboyle.oktane.reactive.results.Result;

public interface TypeParserResult<T> extends Result {
    T value();
}
