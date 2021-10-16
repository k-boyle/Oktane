package com.github.kboyle.oktane.core.annotation;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface AnnotationConsumer<VALUE, ANNOTATION extends Annotation> extends BiConsumer<ANNOTATION, Consumer<VALUE>> {
    Class<ANNOTATION> annotationClass();

    default Optional<Class<? extends Annotation>> repeatableClass() {
        return Optional.empty();
    }
}
