package com.github.kboyle.oktane.core.precondition;

import com.github.kboyle.oktane.core.annotation.AnnotationConsumer;

import java.lang.annotation.Annotation;

public interface ParameterPreconditionAnnotationConsumer<ANNOTATION extends Annotation> extends AnnotationConsumer<ParameterPrecondition<?>, ANNOTATION> {
}
