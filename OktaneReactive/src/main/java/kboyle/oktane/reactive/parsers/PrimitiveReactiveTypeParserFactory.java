package kboyle.oktane.reactive.parsers;

import com.google.common.collect.ImmutableMap;
import kboyle.oktane.reactive.generation.RuntimeClassFactory;

public final class PrimitiveReactiveTypeParserFactory {
    private PrimitiveReactiveTypeParserFactory() {
    }

    private static final String TEMPLATE = """
        package kboyle.oktane.reactive.parsers;
        
        import kboyle.oktane.reactive.CommandContext;
        import kboyle.oktane.reactive.module.ReactiveCommand;
        import kboyle.oktane.reactive.results.typeparser.TypeParserResult;
        import reactor.core.publisher.Mono;
        
        public final class %1$sTypeParser implements ReactiveTypeParser<%1$s> {
            private final Class<%1$s> clazz;

            public %1$sTypeParser(Class<%1$s> clazz) {
                this.clazz = clazz;
            }

            @Override
            public Mono<TypeParserResult<%1$s>> parse(CommandContext context, ReactiveCommand command, String input) {
                try {
                    %1$s value = %1$s.parse%2$s(input);
                    return monoSuccess(value);
                } catch (Exception e) {
                    return monoFailure("Failed to parse \\"%%s\\" as %%s", input, clazz);
                }
            }
        }
        """;

    private static <T> ReactiveTypeParser<T> createParser(Class<T> clazz) {
        return createParser(clazz, clazz.getSimpleName());
    }

    @SuppressWarnings("unchecked")
    private static <T> ReactiveTypeParser<T> createParser(Class<T> clazz, String parseMethod) {
        String code = String.format(TEMPLATE, clazz.getSimpleName(), parseMethod);
        return RuntimeClassFactory.compile(ReactiveTypeParser.class, clazz.getSimpleName(), code, new Object[] { clazz });
    }

    public static ImmutableMap<Class<?>, ReactiveTypeParser<?>> create() {
        return ImmutableMap.<Class<?>, ReactiveTypeParser<?>>builder()
            .put(boolean.class, createParser(Boolean.class))
            .put(Boolean.class, createParser(Boolean.class))

            .put(byte.class, createParser(Byte.class))
            .put(Byte.class, createParser(Byte.class))

            .put(char.class, new CharReactiveTypeParser())
            .put(Character.class, new CharReactiveTypeParser())

            .put(int.class, createParser(Integer.class, "Int")) // consistency :)
            .put(Integer.class, createParser(Integer.class, "Int"))

            .put(short.class, createParser(Short.class))
            .put(Short.class, createParser(Short.class))

            .put(float.class, createParser(Float.class))
            .put(Float.class, createParser(Float.class))

            .put(long.class, createParser(Long.class))
            .put(Long.class, createParser(Long.class))

            .put(double.class, createParser(Double.class))
            .put(Double.class, createParser(Double.class))
            .build();
    }
}
