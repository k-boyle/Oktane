package kboyle.oktane.discord4j.module;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import kboyle.oktane.core.module.ModuleBase;
import kboyle.oktane.core.results.command.CommandResult;
import kboyle.oktane.discord4j.DiscordCommandContext;
import kboyle.oktane.discord4j.results.DiscordMessageResult;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

public abstract class DiscordModuleBase<CONTEXT extends DiscordCommandContext> extends ModuleBase<CONTEXT> {
    protected Mono<CommandResult> reply(String content) {
        return context().channel()
            .map(channel ->
                new DiscordMessageResult(
                    context().command(),
                    channel,
                    spec -> spec.setContent(content)
                        .setMessageReference(context().message().getId())
                )
            );
    }

    protected Mono<CommandResult> reply(Consumer<MessageCreateSpec> messageCreateSpecConsumer) {
        var withReference = messageCreateSpecConsumer.andThen(spec -> spec.setMessageReference(context().message().getId()));
        return context().channel()
            .map(channel ->
                new DiscordMessageResult(
                    context().command(),
                    channel,
                    withReference
                )
            );
    }

    protected Mono<CommandResult> embed(Consumer<EmbedCreateSpec> embedCreateSpecConsumer) {
        return context().channel()
            .map(channel ->
                new DiscordMessageResult(
                    context().command(),
                    channel,
                    spec -> spec.setEmbed(embedCreateSpecConsumer)
                        .setMessageReference(context().message().getId())
                )
            );
    }
}
