package com.github.kboyle.oktane.core.parsing;

import com.github.kboyle.oktane.core.command.CommandParameter;
import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.result.typeparser.*;
import com.google.common.base.Preconditions;

import java.util.function.Function;
import java.util.stream.Stream;

public interface TypeParser<T> {
    Class<T> targetType();
    TypeParserResult<T> parse(CommandContext context, CommandParameter<T> parameter, String input);

    default TypeParserResult<T> success(T value) {
        return new TypeParserSuccessfulResult<>(value, this);
    }

    default TypeParserResult<T> failure(String reason, Object... args) {
        return new TypeParserFailResult<>(this, String.format(reason, args));
    }

    default TypeParser<T> fallback(TypeParser<T> fallback) {
        return new FallbackTypeParser<>(this, fallback);
    }

    static <T> TypeParser<T> simple(Class<T> targetType, Function<String, T> mapperFunction) {
        return new SimpleTypeParser<>(targetType, mapperFunction);
    }

    static <T extends Enum<T>> TypeParser<T> forEnum(Class<T> enumClass) {
        return new EnumTypeParser<>(enumClass);
    }

    static Stream<TypeParser<?>> getDefaultTypeParsers() {
        Function<String, Character> charParseFunction = str -> {
            Preconditions.checkState(str.length() == 1);
            return str.charAt(0);
        };

        return Stream.<TypeParser<?>>builder()
            .add(simple(String.class, Function.identity()))
            .add(simple(Boolean.class, Boolean::parseBoolean))
            .add(simple(boolean.class, Boolean::parseBoolean))
            .add(simple(Byte.class, Byte::parseByte))
            .add(simple(byte.class, Byte::parseByte))
            .add(simple(Character.class, charParseFunction))
            .add(simple(char.class, charParseFunction))
            .add(simple(Integer.class, Integer::parseInt))
            .add(simple(int.class, Integer::parseInt))
            .add(simple(Short.class, Short::parseShort))
            .add(simple(short.class, Short::parseShort))
            .add(simple(Float.class, Float::parseFloat))
            .add(simple(float.class, Float::parseFloat))
            .add(simple(Long.class, Long::parseLong))
            .add(simple(long.class, Long::parseLong))
            .add(simple(Double.class, Double::parseDouble))
            .add(simple(double.class, Double::parseDouble))
            .build();
    }
}
