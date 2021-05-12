package kboyle.oktane.discord4j;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TextChannel;
import kboyle.oktane.core.BeanProvider;
import kboyle.oktane.core.CommandContext;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class DiscordCommandContext extends CommandContext {
    private final Message message;
    private final GatewayDiscordClient client;

    public DiscordCommandContext(Message message) {
        this(message, BeanProvider.empty());
    }

    public DiscordCommandContext(Message message, BeanProvider beanProvider) {
        super(beanProvider);
        this.message = message;
        this.client = message.getClient();
    }

    public Message message() {
        return message;
    }

    public GatewayDiscordClient client() {
        return client;
    }

    public Mono<MessageChannel> channel() {
        return message.getChannel();
    }

    public Mono<TextChannel> textChannel() {
        return message.getChannel().ofType(TextChannel.class);
    }

    public Optional<User> user() {
        return message.getAuthor();
    }

    public Mono<Member> member() {
        return message.getAuthorAsMember();
    }

    public Mono<Guild> guild() {
        return member().flatMap(Member::getGuild);
    }
}
