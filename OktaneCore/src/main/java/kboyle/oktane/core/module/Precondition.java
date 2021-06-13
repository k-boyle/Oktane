package kboyle.oktane.core.module;

import com.google.common.base.Preconditions;
import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.results.precondition.PreconditionFailedResult;
import kboyle.oktane.core.results.precondition.PreconditionResult;
import kboyle.oktane.core.results.precondition.PreconditionSuccessfulResult;
import reactor.core.publisher.Mono;

import java.lang.reflect.AnnotatedElement;
import java.util.Set;

@FunctionalInterface
public interface Precondition {
    Mono<PreconditionResult> run(CommandContext context, Command command);

    default PreconditionResult success() {
        return PreconditionSuccessfulResult.get();
    }

    default PreconditionResult failure(String reason, Object... args) {
        return new PreconditionFailedResult(String.format(reason, args));
    }

    /**
     * @return A set of {@link AnnotatedElement}s that this precondition supports being added to.
     */
    default Set<AnnotatedElement> supportedTargets() {
        return Set.of();
    }

    default void validate(AnnotatedElement annotatedElement) {
        Preconditions.checkNotNull(annotatedElement, "annotatedElement cannot be null");
        var supportedTypes = Preconditions.checkNotNull(supportedTargets(), "supportedTargets() cannot be null");
        if (!supportedTypes.isEmpty()) {
            Preconditions.checkState(supportedTypes.contains(annotatedElement), "%s does not support %s", this, annotatedElement);
        }
    }
}
