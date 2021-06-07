package kboyle.oktane.discord4j.precondition;

import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.Precondition;
import kboyle.oktane.core.module.factory.PreconditionFactory;
import kboyle.oktane.core.processor.ConfigureWith;
import kboyle.oktane.core.results.precondition.PreconditionResult;
import kboyle.oktane.discord4j.DiscordCommandContext;
import reactor.core.publisher.Mono;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.BiConsumer;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RequireBotOwner {
    String group() default "";

    class BotOwnerPrecondition extends DiscordPrecondition {
        @Override
        public Mono<PreconditionResult> run(DiscordCommandContext context, Command command) {
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

    @ConfigureWith(priority = -1)
    class Factory extends PreconditionFactory<RequireBotOwner> {
        @Override
        public Class<RequireBotOwner> supportedType() {
            return RequireBotOwner.class;
        }

        @Override
        public void createGrouped(RequireBotOwner annotation, BiConsumer<Object, Precondition> preconditionConsumer) {
            preconditionConsumer.accept(annotation.group(), new BotOwnerPrecondition());
        }
    }
}
