package kboyle.oktane.core.parsers;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.results.typeparser.TypeParserResult;
import reactor.core.publisher.Mono;

public class EnumTypeParser<T extends Enum<T>> implements TypeParser<T> {
    private final Class<T> enumClazz;
    private final T[] enumConstants;

    public EnumTypeParser(Class<T> enumClazz) {
        this.enumClazz = enumClazz;
        this.enumConstants = enumClazz.getEnumConstants();
    }

    @Override
    public Mono<TypeParserResult<T>> parse(CommandContext context, Command command, String input) {
        return parse(input).mono();
    }

    private TypeParserResult<T> parse(String input) {
        if (Character.isDigit(input.charAt(0))) {
            try {
                int ord = Integer.parseInt(input);
                if (ord >= enumConstants.length) {
                    return failure(
                        "%d is outside of ordinal range for %s [0, %d]",
                        ord,
                        enumClazz.getSimpleName(),
                        enumConstants.length - 1
                    );
                }

                return success(enumConstants[ord]);
            } catch (NumberFormatException ignore) {
                return failure("Failed to parse %s as %s", input, enumClazz.getSimpleName());
            }
        }

        try {
            return success(Enum.valueOf(enumClazz, input.toUpperCase()));
        } catch (IllegalArgumentException exception) {
            return failure("Failed to parse %s as %s", input, enumClazz.getSimpleName());
        }
    }
}
