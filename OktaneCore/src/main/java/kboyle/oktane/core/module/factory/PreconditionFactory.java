package kboyle.oktane.core.module.factory;

import com.google.common.base.Preconditions;
import kboyle.oktane.core.module.Precondition;

import java.lang.annotation.Annotation;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Representations a factory for turning a given {@link Annotation} into a {@link Precondition}.
 *
 * @param <T> The type of {@link Annotation} that this factory is for.
 */
public abstract class PreconditionFactory<T extends Annotation> {
    /**
     * The token that the library uses to represent an ungroup {@link Precondition}.
     */
    public static final Object NO_GROUP = "";

    /**
     * @return The type of {@link Annotation} that this factory supports.
     */
    public abstract Class<T> supportedType();

    /**
     * Allows creation of ungrouped {@link Precondition}s.
     *
     * @param annotation The {@link Annotation} to turn into a precondition.
     * @param preconditionConsumer The {@link Precondition} consumer, call this to add your precondition.
     */
    public void createUngrouped(T annotation, Consumer<Precondition> preconditionConsumer) {
    }

    /**
     * Allows creation of grouped {@link Precondition}s.
     *
     * @param annotation The {@link Annotation} to turn into a precondition.
     * @param preconditionConsumer The {@link Precondition} consumer, call this to add your precondition,
     *                             the first argument is the group.
     */
    public void createGrouped(T annotation, BiConsumer<Object, Precondition> preconditionConsumer) {
        createUngrouped(annotation, precondition -> preconditionConsumer.accept(NO_GROUP, precondition));
    }

    final void createPrecondition(Annotation annotation, BiConsumer<Object, Precondition> preconditionConsumer) {
        var type = Preconditions.checkNotNull(supportedType(), "supportedType cannot be null");
        Preconditions.checkState(type.isInstance(annotation), "annotation is not an instance of %s", type);
        createGrouped(type.cast(annotation), (group, precondition) -> {
            Preconditions.checkNotNull(group, "Precondition group cannot be null");
            Preconditions.checkNotNull(precondition, "precondition cannot be null");
            preconditionConsumer.accept(group, precondition);
        });
    }
}
