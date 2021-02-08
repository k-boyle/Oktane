package kboyle.oktane.core;

@FunctionalInterface
public interface BeanProvider {
     class EmptyBeanProvider implements BeanProvider {
        private static final BeanProvider INSTANCE = new EmptyBeanProvider();

        private EmptyBeanProvider() {
        }

        @Override
        public <T> T getBean(Class<T> clazz) {
            return null;
        }
    }

    static BeanProvider get() {
        return EmptyBeanProvider.INSTANCE;
    }

    <T> T getBean(Class<T> clazz);
}
