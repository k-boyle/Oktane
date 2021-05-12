package kboyle.oktane.discord4j;

import com.google.common.collect.ImmutableList;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.*;
import kboyle.oktane.core.CommandHandler;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.CommandModule;
import kboyle.oktane.core.results.Result;
import kboyle.oktane.discord4j.parsers.ChannelTypeParser;
import kboyle.oktane.discord4j.parsers.RoleTypeParser;
import kboyle.oktane.discord4j.parsers.UserTypeParser;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.stream.Stream;

public class DiscordCommandHandler<CONTEXT extends DiscordCommandContext> {
    private final CommandHandler<CONTEXT> commandHandler;

    private DiscordCommandHandler(CommandHandler<CONTEXT> commandHandler) {
        this.commandHandler = commandHandler;
    }

    public static <CONTEXT extends DiscordCommandContext> DiscordCommandHandler<CONTEXT> create(Consumer<CommandHandler.Builder<CONTEXT>> commandHandlerConsumer) {
        var builder = CommandHandler.<CONTEXT>builder()
            .withTypeParser(Channel.class, new ChannelTypeParser<>(Channel.class))
            .withTypeParser(TextChannel.class, new ChannelTypeParser<>(TextChannel.class))
            .withTypeParser(MessageChannel.class, new ChannelTypeParser<>(MessageChannel.class))
            .withTypeParser(PrivateChannel.class, new ChannelTypeParser<>(PrivateChannel.class))
            .withTypeParser(GuildChannel.class, new ChannelTypeParser<>(GuildChannel.class))
            .withTypeParser(NewsChannel.class, new ChannelTypeParser<>(NewsChannel.class))
            .withTypeParser(VoiceChannel.class, new ChannelTypeParser<>(VoiceChannel.class))
            .withTypeParser(User.class, new UserTypeParser<>(User.class))
            .withTypeParser(Member.class, new UserTypeParser<>(Member.class))
            .withTypeParser(Role.class, new RoleTypeParser<>());
        commandHandlerConsumer.accept(builder);
        return new DiscordCommandHandler<>(builder.build());
    }

    public CommandHandler<CONTEXT> innerHandler() {
        return this.commandHandler;
    }

    public Mono<Result> execute(String input, CONTEXT context) {
        return commandHandler.execute(input, context);
    }

    public Mono<Result> execute(String input, CONTEXT context, int startIndex) {
        return commandHandler.execute(input, context, startIndex);
    }

    public ImmutableList<CommandModule> modules() {
        return commandHandler.modules();
    }

    public Stream<CommandModule> flattenModules() {
        return commandHandler.flattenModules();
    }

    public Stream<Command> commands() {
        return commandHandler.commands();
    }
}
