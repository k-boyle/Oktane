package kboyle.oktane.core.module.factory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import kboyle.oktane.core.module.Precondition;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A map for mapping {@link Annotation}s to {@link PreconditionFactory}s.
 */
public class PreconditionFactoryMap {
    private final Map<Class<?>, PreconditionFactory<?>> preconditionFactoryByClass;
    private final Map<Class<?>, Class<?>> annotationClassByRepeatingClass;

    public PreconditionFactoryMap() {
        this.preconditionFactoryByClass = new HashMap<>();
        this.annotationClassByRepeatingClass = new HashMap<>();
    }

    public PreconditionFactoryMap(
            Map<Class<?>, PreconditionFactory<?>> preconditionFactoryByClass,
            Map<Class<?>, Class<?>> annotationClassByRepeatingClass) {
        this.preconditionFactoryByClass = ImmutableMap.copyOf(preconditionFactoryByClass);
        this.annotationClassByRepeatingClass = ImmutableMap.copyOf(annotationClassByRepeatingClass);
    }

    /**
     * Adds the given {@code factory} to the map.
     *
     * @param factory The factory to add.
     */
    public void put(PreconditionFactory<?> factory) {
        Preconditions.checkNotNull(factory, "factory cannot be null");
        var supportedType = Preconditions.checkNotNull(factory.supportedType(), "supportedType cannot be null");
        var repeatable = supportedType.getAnnotation(Repeatable.class);

        if (repeatable != null) {
            annotationClassByRepeatingClass.put(repeatable.value(), supportedType);
        }

        preconditionFactoryByClass.put(supportedType, factory);
    }

    /**
     * Calls the {@link PreconditionFactory} for the given {@code annotation}, if applicable.
     *
     * @param annotation The {@link Annotation} to handle.
     * @param preconditionConsumer The consumer to pass into the {@link PreconditionFactory}
     */
    public void handle(Annotation annotation, BiConsumer<Object, Precondition> preconditionConsumer) {
        var annotationType = annotation.annotationType();
        var repeatedType = annotationClassByRepeatingClass.get(annotationType);

        if (repeatedType != null) {
            try {
                var valueMethod = annotationType.getMethod("token");
                var annotations = (Annotation[]) valueMethod.invoke(annotation);
                for (var repeatedAnnotation : annotations) {
                    handle(repeatedAnnotation, preconditionConsumer);
                }
            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException ignore) {
            }
        } else {
            var factory = preconditionFactoryByClass.get(annotationType);
            if (factory != null) {
                factory.createPrecondition(annotation, preconditionConsumer);
            }
        }
    }

    /**
     * Copies the map.
     *
     * @return An immutable copy of the map.
     */
    public PreconditionFactoryMap copy() {
        return new PreconditionFactoryMap(preconditionFactoryByClass, annotationClassByRepeatingClass);
    }
}