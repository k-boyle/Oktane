package kboyle.oktane.core.precondition;

import com.google.common.base.Preconditions;
import kboyle.oktane.core.module.Precondition;

import java.lang.annotation.Annotation;

public abstract class PreconditionFactory<T extends Annotation> {
    public abstract Class<T> annotationType();

    public abstract Precondition createPrecondition(T annotation);

    public final Precondition createPrecondition0(Annotation annotation) {
        Preconditions.checkState(annotationType().isInstance(annotation), "annotation is not an instance of %s", annotationType());
        return createPrecondition(annotationType().cast(annotation));
    }
}
