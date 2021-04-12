package kboyle.oktane.reactive;

import kboyle.oktane.reactive.exceptions.UnhandledTypeException;
import kboyle.oktane.reactive.module.CommandModuleBase;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public final class ReflectionUtil {
    private ReflectionUtil() {
    }

    public static ParameterizedType unwrap(Type type) {
        if (type instanceof Class<?> clazz) {
            return unwrap(clazz.getGenericSuperclass());
        } else if (type instanceof ParameterizedType parameterizedType) {
            if (parameterizedType.getRawType() == CommandModuleBase.class) {
                return parameterizedType;
            }
            return unwrap(parameterizedType);
        }

        throw new UnhandledTypeException(type);
    }

    public static <T extends CommandContext> boolean isValidModuleClass(Class<T> contextClazz, Class<?> moduleCandidate) {
        if (!CommandModuleBase.class.isAssignableFrom(moduleCandidate) || Modifier.isAbstract(moduleCandidate.getModifiers())) {
            return false;
        }

        ParameterizedType parameterizedType = unwrap(moduleCandidate.getGenericSuperclass());
        if (parameterizedType.getRawType() != CommandModuleBase.class) {
            return isValidModuleClass(contextClazz, (Class<?>) parameterizedType.getRawType());
        }

        return parameterizedType.getActualTypeArguments()[0] == contextClazz;
    }
}
