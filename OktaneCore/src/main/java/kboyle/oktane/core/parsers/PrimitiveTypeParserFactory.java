package kboyle.oktane.core.parsers;

import com.google.common.collect.ImmutableMap;

public final class PrimitiveTypeParserFactory {
    private PrimitiveTypeParserFactory() {
    }

    public static ImmutableMap<Class<?>, TypeParser<?>> create() {
        return ImmutableMap.<Class<?>, TypeParser<?>>builder()
            .put(boolean.class, new PrimitiveTypeParser<>(Boolean.class, Boolean::parseBoolean))
            .put(Boolean.class, new PrimitiveTypeParser<>(Boolean.class, Boolean::parseBoolean))

            .put(byte.class, new PrimitiveTypeParser<>(Byte.class, Byte::parseByte))
            .put(Byte.class, new PrimitiveTypeParser<>(Byte.class, Byte::parseByte))

            .put(char.class, new CharTypeParser())
            .put(Character.class, new CharTypeParser())

            .put(int.class, new PrimitiveTypeParser<>(Integer.class, Integer::parseInt))
            .put(Integer.class, new PrimitiveTypeParser<>(Integer.class, Integer::parseInt))

            .put(short.class, new PrimitiveTypeParser<>(Short.class, Short::parseShort))
            .put(Short.class, new PrimitiveTypeParser<>(Short.class, Short::parseShort))

            .put(float.class, new PrimitiveTypeParser<>(Float.class, Float::parseFloat))
            .put(Float.class, new PrimitiveTypeParser<>(Float.class, Float::parseFloat))

            .put(long.class, new PrimitiveTypeParser<>(Long.class, Long::parseLong))
            .put(Long.class, new PrimitiveTypeParser<>(Long.class, Long::parseLong))

            .put(double.class, new PrimitiveTypeParser<>(Double.class, Double::parseDouble))
            .put(Double.class, new PrimitiveTypeParser<>(Double.class, Double::parseDouble))
            .build();
    }
}
