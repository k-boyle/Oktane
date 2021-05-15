package kboyle.oktane.core.module.factory;

import com.google.common.base.Preconditions;
import kboyle.oktane.core.module.Precondition;

import java.lang.annotation.Annotation;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class PreconditionFactory<T extends Annotation> {
    public static final Object NO_GROUP = "";

    public abstract Class<T> supportedType();

    public void createUngrouped(T annotation, Consumer<Precondition> preconditionConsumer) {
    }

    public void createGrouped(T annotation, BiConsumer<Object, Precondition> preconditionConsumer) {
        createUngrouped(annotation, precondition -> preconditionConsumer.accept(NO_GROUP, precondition));
    }

    final void createPrecondition0(Annotation annotation, BiConsumer<Object, Precondition> preconditionConsumer) {
        var type = Preconditions.checkNotNull(supportedType(), "supportedType cannot be null");
        Preconditions.checkState(type.isInstance(annotation), "annotation is not an instance of %s", type);
        createGrouped(type.cast(annotation), (group, precondition) -> {
            Preconditions.checkNotNull(group, "Precondition group cannot be null");
            Preconditions.checkNotNull(precondition, "precondition cannot be null");
            preconditionConsumer.accept(group, precondition);
        });
    }
}
