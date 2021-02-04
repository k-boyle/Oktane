package kboyle.octane.core.parsers;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import kboyle.octane.core.CommandContext;
import kboyle.octane.core.results.typeparser.TypeParserResult;

import java.util.function.Function;

public class PrimitiveTypeParser<T> implements TypeParser<T> {
    private static final Function<String, Character> PARSE_CHAR = input ->  {
        Preconditions.checkState(input.length() == 1);
        return input.charAt(0);
    };

    public static final ImmutableMap<Class<?>, TypeParser<?>> DEFAULT_PARSERS = ImmutableMap.<Class<?>, TypeParser<?>>builder()
        .put(boolean.class, new PrimitiveTypeParser<>(Boolean::parseBoolean, boolean.class))
        .put(Boolean.class, new PrimitiveTypeParser<>(Boolean::parseBoolean, Boolean.class))

        .put(byte.class, new PrimitiveTypeParser<>(Byte::parseByte, byte.class))
        .put(Byte.class, new PrimitiveTypeParser<>(Byte::parseByte, Byte.class))

        .put(char.class, new PrimitiveTypeParser<>(PARSE_CHAR, char.class))
        .put(Character.class, new PrimitiveTypeParser<>(PARSE_CHAR, Character.class))

        .put(int.class, new PrimitiveTypeParser<>(Integer::parseInt, int.class))
        .put(Integer.class, new PrimitiveTypeParser<>(Integer::parseInt, Integer.class))

        .put(short.class, new PrimitiveTypeParser<>(Short::parseShort, short.class))
        .put(Short.class, new PrimitiveTypeParser<>(Short::parseShort, Short.class))

        .put(float.class, new PrimitiveTypeParser<>(Float::parseFloat, float.class))
        .put(Float.class, new PrimitiveTypeParser<>(Float::parseFloat, Float.class))

        .put(long.class, new PrimitiveTypeParser<>(Long::parseLong, long.class))
        .put(Long.class, new PrimitiveTypeParser<>(Long::parseLong, Long.class))

        .put(double.class, new PrimitiveTypeParser<>(Double::parseDouble, double.class))
        .put(Double.class, new PrimitiveTypeParser<>(Double::parseDouble, Double.class))
        .build();

    private final Function<String, T> parseFunction;
    private final Class<T> clazz;

    private PrimitiveTypeParser(Function<String, T> parseFunction, Class<T> clazz) {
        this.parseFunction = parseFunction;
        this.clazz = clazz;
    }

    @Override
    public TypeParserResult parse(CommandContext context, String input) {
        try {
            T value = parseFunction.apply(input);
            return success(value);
        } catch (Exception e) {
            return failure("Failed to parse %s as %s", input, clazz);
        }
    }
}
