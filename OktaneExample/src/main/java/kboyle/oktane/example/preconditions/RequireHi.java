package kboyle.oktane.example.preconditions;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.Precondition;
import kboyle.oktane.core.module.factory.PreconditionFactory;
import kboyle.oktane.core.processor.ConfigureWith;
import kboyle.oktane.core.results.precondition.PreconditionResult;
import reactor.core.publisher.Mono;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.BiConsumer;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Repeatable(RequireHi.Repeatable.class)
public @interface RequireHi {
    String value();
    String group() default "";

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    @interface Repeatable {
        RequireHi[] value();
    }

    class HiPrecondition implements Precondition {
        private final String something;

        public HiPrecondition(String something) {
            this.something = something;
        }

        @Override
        public Mono<PreconditionResult> run(CommandContext context, Command command) {
            return (something.equals("hi") ? success() : failure("something wrong")).mono();
        }
    }

    @ConfigureWith
    class Factory extends PreconditionFactory<RequireHi> {
        @Override
        public Class<RequireHi> supportedType() {
            return RequireHi.class;
        }

        @Override
        public void createGrouped(RequireHi annotation, BiConsumer<Object, Precondition> preconditionConsumer) {
            preconditionConsumer.accept(annotation.group(), new HiPrecondition(annotation.value()));
        }
    }
}

