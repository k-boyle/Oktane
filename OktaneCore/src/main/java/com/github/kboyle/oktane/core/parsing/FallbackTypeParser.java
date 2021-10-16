package com.github.kboyle.oktane.core.parsing;

import com.github.kboyle.oktane.core.command.CommandParameter;
import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.result.typeparser.TypeParserResult;
import com.google.common.base.Preconditions;

class FallbackTypeParser<T> implements TypeParser<T> {
    private final TypeParser<T> original;
    private final TypeParser<T> fallback;

    FallbackTypeParser(TypeParser<T> original, TypeParser<T> fallback) {
        this.original = Preconditions.checkNotNull(original, "original cannot be null");;
        this.fallback = Preconditions.checkNotNull(fallback, "fallback cannot be null");;
    }

    @Override
    public Class<T> targetType() {
        return original.targetType();
    }

    @Override
    public TypeParserResult<T> parse(CommandContext context, CommandParameter<T> parameter, String input) {
        var result = Preconditions.checkNotNull(original.parse(context, parameter, input), "parse cannot be null");

        if (result.success()) {
            return result;
        }

        return fallback.parse(context, parameter, input);
    }
}
