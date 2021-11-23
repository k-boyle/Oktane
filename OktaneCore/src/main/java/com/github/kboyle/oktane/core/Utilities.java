package com.github.kboyle.oktane.core;

import com.google.common.base.Preconditions;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.function.*;
import java.util.stream.Stream;

public final class Utilities {
    private Utilities() {
        throw new UnsupportedOperationException("Instantiation of a utility class");
    }

    public static final class Functions {
        private Functions() {
            throw new UnsupportedOperationException("Instantiation of a utility class");
        }

        private static final Consumer<?> CONSUMER_DO_NOTHING = ignore -> { };
        private static final Supplier<?> SUPPLIER_SUPPLY_NULL = () -> null;
        private static final IntSupplier ZERO = () -> 0;
        private static final Runnable RUNNABLE_DO_NOTHING = () -> { };

        @SuppressWarnings("unchecked")
        public static <T> Consumer<T> doNothing() {
            return (Consumer<T>) CONSUMER_DO_NOTHING;
        }

        @SuppressWarnings("unchecked")
        public static <T> Supplier<T> supplyNull() {
            return (Supplier<T>) SUPPLIER_SUPPLY_NULL;
        }

        public static Runnable runNothing() {
            return RUNNABLE_DO_NOTHING;
        }

        public static IntSupplier zero() {
            return ZERO;
        }
    }

    public static final class Objects {
        private static final Map<Class<?>, Class<?>> BOXED_CLASS_BY_PRIMITIVE = Map.of(
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            char.class, Character.class,
            int.class, Integer.class,
            short.class, Short.class,
            float.class, Float.class,
            long.class, Long.class,
            double.class, Double.class
        );

        private Objects() {
            throw new UnsupportedOperationException("Instantiation of a utility class");
        }

        public static <T> T getIfNull(T obj, Supplier<T> or) {
            return obj != null ? obj : or.get();
        }

        public static boolean isBoxedPrimitive(Class<?> cl, Object obj) {
            return BOXED_CLASS_BY_PRIMITIVE.getOrDefault(cl, Objects.class).isInstance(obj);
        }
    }

    public static final class Streams {
        private Streams() {
            throw new UnsupportedOperationException("Instantiation of a utility class");
        }

        public static <T> T single(Stream<T> stream) {
            return stream.reduce((left, right) -> { throw new IllegalStateException("Expected only a single element"); })
                .orElseThrow(() -> new IllegalStateException("Expected a single element"));
        }
    }

    public static final class Spring {
        private Spring() {
            throw new UnsupportedOperationException("Instantiation of a utility class");
        }

        public static Object[] getBeans(ApplicationContext applicationContext, List<Class<?>> beanClasses) {
            if (beanClasses.isEmpty()) {
                return new Object[0];
            }

            var dependencies = new Object[beanClasses.size()];
            for (var i = 0; i < beanClasses.size(); i++) {
                var dependencyClass = beanClasses.get(i);
                dependencies[i] = Preconditions.checkNotNull(
                    applicationContext.getBean(dependencyClass),
                    "A bean of type %s must be registered",
                    dependencyClass
                );
            }

            return dependencies;
        }
    }
}
