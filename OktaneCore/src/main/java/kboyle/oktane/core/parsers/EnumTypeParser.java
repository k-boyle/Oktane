package kboyle.oktane.core.parsers;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.results.typeparser.TypeParserResult;
import reactor.core.publisher.Mono;

public class EnumTypeParser<T extends Enum<T>> implements TypeParser<T> {
    private final Class<T> enumClass;
    private final T[] enumConstants;

    public EnumTypeParser(Class<T> enumClass) {
        this.enumClass = enumClass;
        this.enumConstants = enumClass.getEnumConstants();
    }

    @Override
    public Mono<TypeParserResult<T>> parse(CommandContext context, Command command, String input) {
        return parse(input).mono();
    }

    private TypeParserResult<T> parse(String input) {
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
