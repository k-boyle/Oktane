package kboyle.oktane.example.preconditions;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.Precondition;
import kboyle.oktane.core.module.factory.PreconditionFactory;
import kboyle.oktane.core.results.precondition.PreconditionResult;
import reactor.core.publisher.Mono;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Consumer;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RequireFailure {
    int value();

    class FailurePrecondition implements Precondition {
        private final int a;

        public FailurePrecondition(int a) {
            this.a = a;
        }

        @Override
        public Mono<PreconditionResult> run(CommandContext context, Command command) {
            return failure("Failed because: %d", a).mono();
        }
    }

    class Factory extends PreconditionFactory<RequireFailure> {
        @Override
        public Class<RequireFailure> supportedType() {
            return RequireFailure.class;
        }

        @Override
        public void createUngrouped(RequireFailure annotation, Consumer<Precondition> preconditionConsumer) {
            preconditionConsumer.accept(new FailurePrecondition(annotation.value()));
        }
    }
}
