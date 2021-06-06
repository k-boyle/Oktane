package kboyle.oktane.discord4j.precondition;

import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.Precondition;
import kboyle.oktane.core.module.factory.PreconditionFactory;
import kboyle.oktane.core.processor.AutoWith;
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
public @interface RequireGuildOwner {
    String group();

    class GuildOwnerPrecondition extends DiscordPrecondition {
        @Override
        public Mono<PreconditionResult> run(DiscordCommandContext context, Command command) {
            return context.guild()
                .map(guild -> context.user()
                    .map(author -> {
                        if (author.getId().equals(guild.getId())) {
                            return success();
                        }

                        return failure("Only the guild owner can execute this command.");
                    })
                    .orElseGet(() -> failure("Missing message author on message %s", context.message().getId()))
                );
        }
    }

    @AutoWith
    class Factory extends PreconditionFactory<RequireGuildOwner> {
        @Override
        public Class<RequireGuildOwner> supportedType() {
            return RequireGuildOwner.class;
        }

        @Override
        public void createGrouped(RequireGuildOwner annotation, BiConsumer<Object, Precondition> preconditionConsumer) {
            preconditionConsumer.accept(annotation.group(), new GuildOwnerPrecondition());
        }
    }
}
