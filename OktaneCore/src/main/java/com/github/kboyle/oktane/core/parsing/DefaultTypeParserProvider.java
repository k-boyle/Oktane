package com.github.kboyle.oktane.core.parsing;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;

class DefaultTypeParserProvider implements TypeParserProvider {
    private final Map<Class<?>, TypeParser<?>> typeParserByClass;

    DefaultTypeParserProvider(Map<Class<?>, TypeParser<?>> typeParserByClass) {
        this.typeParserByClass = Map.copyOf(typeParserByClass);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeParser<T> get(Class<T> cl) {
        var parser = Preconditions.checkNotNull(typeParserByClass.get(cl), "Missing type parser for type %s", cl);
        Preconditions.checkNotNull(parser.targetType(), "TypeParser#targetType cannot return null");
        Preconditions.checkState(cl == parser.targetType(), "Expected a type parser for type %s but got %s", cl, parser.targetType());
        return (TypeParser<T>) parser;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeParser<T> getOverride(Class<TypeParser<? extends T>> cl) {
        var parser = Preconditions.checkNotNull(typeParserByClass.get(cl), "Missing type parser for type %s", cl);
        Preconditions.checkState(cl == parser.getClass(), "Expected a type parser of type %s but got %s", cl, parser.targetType());
        return (TypeParser<T>) parser;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public <T extends Enum> TypeParser<T> getEnum(Class<T> enumClass) {
        return TypeParser.forEnum(enumClass);
    }

    static class Builder implements TypeParserProvider.Builder {
        private final Map<Class<?>, TypeParser<?>> typeParserByClass;

        public Builder() {
            this.typeParserByClass = new HashMap<>();
        }

        @Override
        public Builder set(TypeParser<?> typeParser) {
            Preconditions.checkNotNull(typeParser, "typeParser cannot be null");
            Preconditions.checkNotNull(typeParser.targetType(), "TypeParser#targetType cannot return null");
            typeParserByClass.put(typeParser.targetType(), typeParser);
            return this;
        }

        @Override
        public Builder add(TypeParser<?> typeParser) {
            Preconditions.checkNotNull(typeParser, "typeParser cannot be null");
            Preconditions.checkNotNull(typeParser.targetType(), "TypeParser#targetType cannot return null");
            typeParserByClass.put(typeParser.getClass(), typeParser);
            return this;
        }

        @Override
        public TypeParserProvider build() {
            return new DefaultTypeParserProvider(typeParserByClass);
        }
    }
}
