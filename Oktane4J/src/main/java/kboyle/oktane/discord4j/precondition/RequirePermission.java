package kboyle.oktane.discord4j.precondition;

import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
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

    class PermissionPrecondition extends DiscordPrecondition {
        private final PermissionSet permissions;
        private final PermissionTarget target;

        public PermissionPrecondition(PermissionTarget target, PermissionSet permissions) {
            this.permissions = permissions;
            this.target = target;
        }

        @Override
        public Mono<PreconditionResult> run(DiscordCommandContext context, Command command) {
            return target.get(context)
                .flatMap(member -> context.textChannel().flatMap(channel -> channel.getEffectivePermissions(member.getId())))
                .map(targetPerms -> {
                    if (targetPerms.and(permissions).equals(permissions)) {
                        return success();
                    }

                    return failure("%s require %s to execute this command", target == PermissionTarget.USER ? "You" : "I", permissions);
                })
                .switchIfEmpty(NOT_IN_GUILD);
        }
    }

    @ConfigureWith(priority = -1)
    class Factory extends PreconditionFactory<RequirePermission> {
        @Override
        public Class<RequirePermission> supportedType() {
            return RequirePermission.class;
        }

        @Override
        public void createGrouped(RequirePermission annotation, BiConsumer<Object, Precondition> preconditionConsumer) {
            var permissions = PermissionSet.of(annotation.permissions());
            preconditionConsumer.accept(annotation.group(), new PermissionPrecondition(annotation.target(), permissions));
        }
    }
}
