package kboyle.oktane.discord4j.module;

import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.AllowedMentions;
import kboyle.oktane.core.module.CommandModule;
import kboyle.oktane.core.module.ModuleBase;
import kboyle.oktane.core.results.command.CommandResult;
import kboyle.oktane.discord4j.DiscordCommandContext;
import kboyle.oktane.discord4j.results.DiscordMessageResult;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

/**
 * Represents a base class to designate a class as a Discord based {@link CommandModule}.
 *
 * @param <CONTEXT> The type of {@link DiscordCommandContext} to use.
 */
public abstract class DiscordModuleBase<CONTEXT extends DiscordCommandContext> extends ModuleBase<CONTEXT> {
    /**
     * Represents a reply to a {@link Message}. Does not mention anyone by default.
     *
     * @param content The message to send.
     * @return A {@link CommandResult} representing a reply to the user.
     */
    protected Mono<CommandResult> reply(String content) {
        return context().channel()
            .map(channel ->
                new DiscordMessageResult(
                    context().command(),
                    channel,
                    baseConsumer().andThen(spec -> spec.setContent(content))
                )
            );
    }

    /**
     * Represents a reply to a {@link Message}. Does not mention anyone by default.
     *
     * @param messageCreateSpecConsumer The spec to use for the {@link Message}.
     * @return A {@link CommandResult} representing a reply to the user.
     */
    protected Mono<CommandResult> reply(Consumer<MessageCreateSpec> messageCreateSpecConsumer) {
        return context().channel()
            .map(channel ->
                new DiscordMessageResult(
                    context().command(),
                    channel,
                    baseConsumer().andThen(messageCreateSpecConsumer)
                )
            );
    }

    /**
     * Represents an {@link Embed} reply to a {@link Message}. Does not mention anyone by default.
     *
     * @param embedCreateSpecConsumer The spec to use for the {@link Embed}.
     * @return A {@link CommandResult} representing an {@link Embed} reply to the user.
     */
    protected Mono<CommandResult> embed(Consumer<EmbedCreateSpec> embedCreateSpecConsumer) {
        return context().channel()
            .map(channel ->
                new DiscordMessageResult(
                    context().command(),
                    channel,
                    baseConsumer().andThen(spec -> spec.setEmbed(embedCreateSpecConsumer))
                )
            );
    }

    private Consumer<MessageCreateSpec> baseConsumer() {
        return spec -> spec.setMessageReference(context().message().getId())
            .setAllowedMentions(AllowedMentions.suppressAll());
    }
}
