package kboyle.oktane.core;

import java.util.HashMap;
import java.util.Map;

/**
 * A container to be used by the {@code CommandHandler} for accessing beans.
 */
@FunctionalInterface
public interface BeanProvider {
    class Empty implements BeanProvider {
        private static final Empty INSTANCE = new Empty();

        private Empty() {
        }

        @Override
        public <T> T getBean(Class<T> cl) {
            return null;
        }
    }

    class Simple implements BeanProvider {
        private final Map<Class<?>, Object> beanByClass;

        private Simple() {
            beanByClass = new HashMap<>();
        }

        public <T> Simple add(Class<T> cl, T bean) {
            beanByClass.put(cl, bean);
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getBean(Class<T> cl) {
            return (T) beanByClass.get(cl);
        }
    }

    /**
     * @return An empty provider.
     */
    static Empty empty() {
        return Empty.INSTANCE;
    }

    /**
     * @return A simple provider.
     */
    static Simple simple() {
        return new Simple();
    }

    /**
     * Gets a bean of the given type from the provider.
     *
     * @param cl The class of the type you want to fetch.
     * @param <T> The type you want.
     * @return The bean corresponding to the given type.
     */
    <T> T getBean(Class<T> cl);
}
