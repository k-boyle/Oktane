package com.github.kboyle.oktane.core.dependency;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;

public class SimpleDependencyProvider implements DependencyProvider {
    private final Map<Class<?>, ?> dependencyByClass;

    public SimpleDependencyProvider(Map<Class<?>, ?> dependencyByClass) {
        Preconditions.checkNotNull(dependencyByClass, "dependencyByClass cannot be null");

        this.dependencyByClass = Map.copyOf(dependencyByClass);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> cl) {
        return (T) dependencyByClass.get(cl);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<Class<?>, Object> dependencyByClass;

        private Builder() {
            this.dependencyByClass = new HashMap<>();
        }

        public Builder dependency(Object dependency) {
            Preconditions.checkNotNull(dependency, "dependency cannot be null");
            dependencyByClass.put(dependency.getClass(), dependency);
            return this;
        }

        public Builder dependencies(Object... dependencies) {
            Preconditions.checkNotNull(dependencies, "dependencies cannot be null");
            for (var dependency : dependencies) {
                dependency(dependency);
            }
            return this;
        }

        public SimpleDependencyProvider build() {
            return new SimpleDependencyProvider(dependencyByClass);
        }
    }
}
