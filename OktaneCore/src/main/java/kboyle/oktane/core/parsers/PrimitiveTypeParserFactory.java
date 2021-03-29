package kboyle.oktane.core.parsers;

import com.google.common.collect.ImmutableMap;
import kboyle.oktane.core.generation.RuntimeClassFactory;

public final class PrimitiveTypeParserFactory {
    private PrimitiveTypeParserFactory() {
    }

    private static final String TEMPLATE = """
        package kboyle.oktane.core.parsers;
        
        import kboyle.oktane.core.CommandContext;
        import kboyle.oktane.core.results.typeparser.TypeParserResult;
        
        public final class %1$sTypeParser implements TypeParser<%1$s> {
            private final Class<%1$s> clazz;
                
            public %1$sTypeParser(Class<%1$s> clazz) {
                this.clazz = clazz;
            }

            @Override
            public TypeParserResult<%1$s> parse(CommandContext context, String input) {
                try {
                    %1$s value = %1$s.parse%2$s(input);
                    return success(value);
                } catch (Exception e) {
                    return failure("Failed to parse %%s as %%s", input, clazz);
                }
            }
        }
        """;

    private static <T> TypeParser<T> createParser(Class<T> clazz) {
        return createParser(clazz, clazz.getSimpleName());
    }

    @SuppressWarnings("unchecked")
    private static <T> TypeParser<T> createParser(Class<T> clazz, String parseMethod) {
        String code = String.format(TEMPLATE, clazz.getSimpleName(), parseMethod);
        return RuntimeClassFactory.compile(TypeParser.class, clazz.getSimpleName(), code, new Object[] { clazz });
    }

    public static ImmutableMap<Class<?>, TypeParser<?>> create() {
        return ImmutableMap.<Class<?>, TypeParser<?>>builder()
            .put(boolean.class, createParser(Boolean.class))
            .put(Boolean.class, createParser(Boolean.class))

            .put(byte.class, createParser(Byte.class))
            .put(Byte.class, createParser(Byte.class))

            .put(char.class, new CharTypeParser())
            .put(Character.class, new CharTypeParser())

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
