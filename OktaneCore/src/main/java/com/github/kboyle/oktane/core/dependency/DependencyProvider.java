package com.github.kboyle.oktane.core.dependency;

@FunctionalInterface
public interface DependencyProvider {
    <T> T get(Class<T> cl);

    static DependencyProvider empty() {
        return EmptyDependencyProvider.get();
    }
}
