package com.github.kboyle.oktane.core.dependency;

class EmptyDependencyProvider implements DependencyProvider {
    private static final EmptyDependencyProvider INSTANCE = new EmptyDependencyProvider();

    private EmptyDependencyProvider() {
    }

    @Override
    public <T> T get(Class<T> cl) {
        return null;
    }

    public static DependencyProvider get() {
        return INSTANCE;
    }
}
