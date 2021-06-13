package kboyle.oktane.discord4j.precondition;

import discord4j.core.object.entity.Member;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.Precondition;
import kboyle.oktane.core.module.factory.PreconditionFactory;
import kboyle.oktane.core.processor.ConfigureWith;
import kboyle.oktane.core.results.precondition.PreconditionResult;
import kboyle.oktane.discord4j.DiscordCommandContext;
import reactor.core.publisher.Mono;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.util.Set;
import java.util.function.BiConsumer;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Repeatable(RequireHierarchy.Repeatable.class)
public @interface RequireHierarchy {
    PermissionTarget target();
    String group() default "";

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    @interface Repeatable {
        RequireHierarchy[] value();
    }

    class HierarchyPrecondition extends DiscordPrecondition {
        private final PermissionTarget target;

        public HierarchyPrecondition(PermissionTarget target) {
            this.target = target;
        }

        @Override
        public Mono<PreconditionResult> run(DiscordCommandContext context, Command command) {
            if (!(context.currentArgument() instanceof Member targetMember)) {
                return failure("This precondition can only be used on a Member parameter").mono();
            }

            return target.get(context)
                .flatMap(member ->
                    member.isHigher(targetMember)
                        .map(res -> {
                            if (res) {
                                return success();
                            }

                            return failure(
                                "%s has a lower hierarchy than %s",
                                member.getDisplayName(),
                                targetMember.getDisplayName()
                            );
                        })
                )
                .switchIfEmpty(NOT_IN_GUILD);
        }

        @Override
        public Set<AnnotatedElement> supportedTargets() {
            return Set.of(Member.class);
        }
    }

    @ConfigureWith(priority = -1)
    class Factory extends PreconditionFactory<RequireHierarchy> {
        @Override
        public Class<RequireHierarchy> supportedType() {
            return RequireHierarchy.class;
        }

        @Override
        public void createGrouped(RequireHierarchy annotation, BiConsumer<Object, Precondition> preconditionConsumer) {
            preconditionConsumer.accept(annotation.group(), new HierarchyPrecondition(annotation.target()));
        }
    }
}
