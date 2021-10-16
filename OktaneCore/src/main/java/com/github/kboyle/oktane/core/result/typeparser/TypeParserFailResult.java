package com.github.kboyle.oktane.core.result.typeparser;

import com.github.kboyle.oktane.core.parsing.TypeParser;
import com.github.kboyle.oktane.core.result.FailResult;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class TypeParserFailResult<T> implements TypeParserResult<T>, FailResult {
    private final TypeParser<T> typeParser;
    private final String failureReason;

    public TypeParserFailResult(TypeParser<T> typeParser, String failureReason) {
        this.typeParser = typeParser;
        this.failureReason = failureReason;
    }

    @Override
    public T value() {
        return null;
    }

    public TypeParser<T> typeParser() {
        return typeParser;
    }

    public String failureReason() {
        return failureReason;
    }
}
