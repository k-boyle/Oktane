package kboyle.oktane.discord4j.configuration;

import com.google.auto.service.AutoService;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.NewsChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import kboyle.oktane.core.CommandHandler;
import kboyle.oktane.core.configuration.CommandHandlerConfigurator;
import kboyle.oktane.discord4j.parsers.ChannelTypeParser;
import kboyle.oktane.discord4j.parsers.RoleTypeParser;
import kboyle.oktane.discord4j.parsers.UserTypeParser;

@AutoService(CommandHandlerConfigurator.class)
public class DiscordCommandHandlerWithDiscordTypeParsers implements CommandHandlerConfigurator {
    @Override
    public void apply(CommandHandler.Builder<?> commandHandler) {
        commandHandler.withTypeParser(Channel.class, new ChannelTypeParser<>(Channel.class))
            .withTypeParser(TextChannel.class, new ChannelTypeParser<>(TextChannel.class))
            .withTypeParser(MessageChannel.class, new ChannelTypeParser<>(MessageChannel.class))
            .withTypeParser(PrivateChannel.class, new ChannelTypeParser<>(PrivateChannel.class))
            .withTypeParser(GuildChannel.class, new ChannelTypeParser<>(GuildChannel.class))
            .withTypeParser(NewsChannel.class, new ChannelTypeParser<>(NewsChannel.class))
            .withTypeParser(VoiceChannel.class, new ChannelTypeParser<>(VoiceChannel.class))
            .withTypeParser(User.class, new UserTypeParser<>(User.class))
            .withTypeParser(Member.class, new UserTypeParser<>(Member.class))
            .withTypeParser(Role.class, new RoleTypeParser());
    }
}
