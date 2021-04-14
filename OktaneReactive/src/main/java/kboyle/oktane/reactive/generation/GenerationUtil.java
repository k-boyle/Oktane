package kboyle.oktane.reactive.generation;

import kboyle.oktane.reactive.exceptions.UnhandledTypeException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.StringJoiner;

final class GenerationUtil {
    private static final String GENERIC_TEMPLATE = "%s<%s>";

    private GenerationUtil() {
    }

    public static String formatType(Type type) {
        if (type instanceof Class<?> clazz) {
            return clazz.getSimpleName();
        } else if (type instanceof ParameterizedType parameterizedType) {
            StringJoiner generics = new StringJoiner(", ");
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            for (Type typeArgument : typeArguments) {
                generics.add(formatType(typeArgument));
            }

            Type rawType = parameterizedType.getRawType();
            return String.format(GENERIC_TEMPLATE, rawType.getTypeName(), generics.toString());
        } else {
            throw new UnhandledTypeException(type);
        }
    }
}
