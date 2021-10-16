package com.github.kboyle.oktane.core.parsing;

public interface TypeParserProvider {
    <T> TypeParser<T> get(Class<T> cl);
    <T> TypeParser<T> getOverride(Class<TypeParser<? extends T>> cl);
    @SuppressWarnings("rawtypes")
    <T extends Enum> TypeParser<T> getEnum(Class<T> enumClass);

    static Builder builder() {
        return new DefaultTypeParserProvider.Builder();
    }

    static TypeParserProvider defaults() {
        return builder()
            .defaults()
            .build();
    }

    interface Builder {
        Builder set(TypeParser<?> typeParser);
        Builder add(TypeParser<?> typeParser);

        default Builder defaults() {
            TypeParser.getDefaultTypeParsers().forEach(this::set);
            return this;
        }

        TypeParserProvider build();
    }
}
