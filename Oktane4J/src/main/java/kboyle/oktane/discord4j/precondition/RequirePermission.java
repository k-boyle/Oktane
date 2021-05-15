package kboyle.oktane.discord4j.precondition;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.Precondition;
import kboyle.oktane.core.module.factory.PreconditionFactory;
import kboyle.oktane.core.results.precondition.PreconditionResult;
import kboyle.oktane.discord4j.DiscordCommandContext;
import reactor.core.publisher.Mono;

import java.lang.annotation.*;
import java.util.function.BiConsumer;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Repeatable(RequirePermission.Repeatable.class)
public @interface RequirePermission {
    PermissionTarget target();
    Permission[] permissions();
    String group() default "";

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.METHOD })
    @interface Repeatable {
        RequirePermission[] value();
    }

    class PermissionPrecondition<CONTEXT extends DiscordCommandContext> extends DiscordPrecondition<CONTEXT> {
        private final PermissionSet permissions;
        private final PermissionTarget target;

        public PermissionPrecondition(PermissionTarget target, PermissionSet permissions) {
            this.permissions = permissions;
            this.target = target;
        }

        @Override
        public Mono<PreconditionResult> run(CONTEXT context, Command command) {
            return getTarget(context)
                .flatMap(member -> context.textChannel().flatMap(channel -> channel.getEffectivePermissions(member.getId())))
                .map(targetPerms -> {
                    if (targetPerms.and(permissions).equals(permissions)) {
                        return success();
                    }

                    return failure("%s require %s to execute this command", target == PermissionTarget.USER ? "You" : "I", permissions);
                })
                .switchIfEmpty(failure("This command can only be executed in a guild").mono());
        }

        private Mono<Member> getTarget(CONTEXT context) {
            return switch (target) {
                case USER -> context.member();
                case BOT -> context.guild().flatMap(Guild::getSelfMember);
            };
        }
    }

    class Factory<CONTEXT extends DiscordCommandContext> extends PreconditionFactory<RequirePermission> {
        @Override
        public Class<RequirePermission> supportedType() {
            return RequirePermission.class;
        }

        @Override
        public void createGrouped(RequirePermission annotation, BiConsumer<Object, Precondition> preconditionConsumer) {
            var permissions = PermissionSet.of(annotation.permissions());
            preconditionConsumer.accept(annotation.group(), new PermissionPrecondition<CONTEXT>(annotation.target(), permissions));
        }
    }
}
