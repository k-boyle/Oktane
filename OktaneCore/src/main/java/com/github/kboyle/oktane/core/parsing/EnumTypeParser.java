package com.github.kboyle.oktane.core.parsing;

import com.github.kboyle.oktane.core.command.CommandParameter;
import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.result.typeparser.TypeParserResult;

class EnumTypeParser<T extends Enum<T>> implements TypeParser<T> {
    private final Class<T> enumClass;
    private final T[] enumConstants;

    EnumTypeParser(Class<T> enumClass) {
        this.enumClass = enumClass;
        this.enumConstants = enumClass.getEnumConstants();
    }

    @Override
    public Class<T> targetType() {
        return enumClass;
    }

    @Override
    public TypeParserResult<T> parse(CommandContext context, CommandParameter<T> parameter, String input) {
        if (Character.isDigit(input.charAt(0))) {
            try {
                var ord = Integer.parseInt(input);
                if (ord >= enumConstants.length) {
                    return failure(
                        "%d is outside of ordinal range for %s [0, %d]",
                        ord,
                        enumClass.getSimpleName(),
                        enumConstants.length - 1
                    );
                }

                return success(enumConstants[ord]);
            } catch (NumberFormatException ignore) {
                return failure("Failed to parse %s as %s", input, enumClass.getSimpleName());
            }
        }

        try {
            return success(Enum.valueOf(enumClass, input.toUpperCase()));
        } catch (IllegalArgumentException exception) {
            return failure("Failed to parse %s as %s", input, enumClass.getSimpleName());
        }
    }
}
