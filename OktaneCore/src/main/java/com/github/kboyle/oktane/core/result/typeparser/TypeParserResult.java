package com.github.kboyle.oktane.core.result.typeparser;

import com.github.kboyle.oktane.core.parsing.TypeParser;
import com.github.kboyle.oktane.core.result.Result;

public interface TypeParserResult<T> extends Result {
    T value();
    TypeParser<T> typeParser();
}
