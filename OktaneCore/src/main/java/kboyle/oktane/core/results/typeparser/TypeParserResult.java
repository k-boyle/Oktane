package kboyle.oktane.core.results.typeparser;

import kboyle.oktane.core.results.Result;

public interface TypeParserResult<T> extends Result {
    T value();
}
