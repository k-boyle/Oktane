package com.github.kboyle.oktane.core.parsing;

import com.github.kboyle.oktane.core.command.CommandParameter;
import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.result.typeparser.TypeParserResult;
import com.google.common.base.Preconditions;

import java.util.function.Function;

class SimpleTypeParser<T> implements TypeParser<T> {
    private final Class<T> targetType;
    private final Function<String, T> parseFunction;

    SimpleTypeParser(Class<T> targetType, Function<String, T> parseFunction) {
        this.targetType = Preconditions.checkNotNull(targetType, "targetType cannot be null");
        this.parseFunction = Preconditions.checkNotNull(parseFunction, "parseFunction cannot be null");
    }

    @Override
    public Class<T> targetType() {
        return targetType;
    }

    @Override
    public TypeParserResult<T> parse(CommandContext context, CommandParameter<T> parameter, String input) {
        try {
            return success(parseFunction.apply(input));
        } catch (Exception ex) {
            return failure("Failed to parse %s as %s", input, targetType.getSimpleName());
        }
    }
}
