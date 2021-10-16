package com.github.kboyle.oktane.core.result.typeparser;

import com.github.kboyle.oktane.core.parsing.TypeParser;
import com.github.kboyle.oktane.core.result.SuccessfulResult;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class TypeParserSuccessfulResult<T> implements TypeParserResult<T>, SuccessfulResult {
    private final T value;
    private final TypeParser<T> typeParser;

    public TypeParserSuccessfulResult(T value, TypeParser<T> typeParser) {
        this.value = value;
        this.typeParser = typeParser;
    }

    public T value() {
        return value;
    }

    public TypeParser<T> typeParser() {
        return typeParser;
    }
}
