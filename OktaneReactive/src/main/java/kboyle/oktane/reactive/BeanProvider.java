package kboyle.oktane.reactive;

import java.util.HashMap;
import java.util.Map;

@FunctionalInterface
public interface BeanProvider {
    class Empty implements BeanProvider {
        private static final Empty INSTANCE = new Empty();

        private Empty() {
        }

        @Override
        public <T> T getBean(Class<T> clazz) {
            return null;
        }
    }

    class Simple implements BeanProvider {
        private final Map<Class<?>, Object> beanByClass;

        private Simple() {
            beanByClass = new HashMap<>();
        }

        public <T> Simple add(Class<T> clazz, T bean) {
            beanByClass.put(clazz, bean);
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getBean(Class<T> clazz) {
            return (T) beanByClass.get(clazz);
        }
    }

    static Empty empty() {
        return Empty.INSTANCE;
    }

    static Simple simple() {
        return new Simple();
    }

    <T> T getBean(Class<T> clazz);
}
