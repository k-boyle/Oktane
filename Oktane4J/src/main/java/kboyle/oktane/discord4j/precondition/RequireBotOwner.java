package kboyle.oktane.discord4j.precondition;

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
public @interface RequireBotOwner {
    class Factory extends PreconditionFactory<RequireBotOwner> {
        @Override
        public Class<RequireBotOwner> annotationType() {
            return RequireBotOwner.class;
        }

        @Override
        public Precondition createPrecondition(RequireBotOwner annotation) {
            return new RequireBotOwnerPrecondition<>();
        }
    }

    class RequireBotOwnerPrecondition<CONTEXT extends DiscordCommandContext> extends DiscordPrecondition<CONTEXT> {
        @Override
        public Mono<PreconditionResult> run(CONTEXT context, Command command) {
            return context.client().getApplicationInfo()
                .map(info -> context.user()
                    .map(author -> {
                        if (author.getId().equals(info.getOwnerId())) {
                            return success();
                        }

                        return failure("Only the bot owner can execute this command");
                    })
                    .orElseGet(() -> failure("Missing message author on message %s", context.message().getId()))
                );
        }
    }
}
