package kboyle.oktane.discord4j.precondition;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.Precondition;
import kboyle.oktane.core.precondition.PreconditionFactory;
import kboyle.oktane.core.results.precondition.PreconditionResult;
import kboyle.oktane.discord4j.DiscordCommandContext;
import reactor.core.publisher.Mono;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface RequirePermission {
    PermissionTarget target();
    Permission[] permissions();

    class Factory extends PreconditionFactory<RequirePermission> {
        @Override
        public Class<RequirePermission> annotationType() {
            return RequirePermission.class;
        }

        @Override
        public Precondition createPrecondition(RequirePermission annotation) {
            // how does this compile??
            return new RequirePermissionPrecondition<>(PermissionSet.of(annotation.permissions()), annotation.target());
        }
    }

    class RequirePermissionPrecondition<CONTEXT extends DiscordCommandContext> extends DiscordPrecondition<CONTEXT> {
        private final PermissionSet permissions;
        private final PermissionTarget target;

        public RequirePermissionPrecondition(PermissionSet permissions, PermissionTarget target) {
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
}
