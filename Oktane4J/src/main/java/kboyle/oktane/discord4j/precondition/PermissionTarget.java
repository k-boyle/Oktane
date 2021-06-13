package kboyle.oktane.discord4j.precondition;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import kboyle.oktane.discord4j.DiscordCommandContext;
import reactor.core.publisher.Mono;

public enum PermissionTarget {
    USER {
        @Override
        public Mono<Member> get(DiscordCommandContext context) {
            return context.member();
        }
    },
    BOT {
        @Override
        public Mono<Member> get(DiscordCommandContext context) {
            return context.guild().flatMap(Guild::getSelfMember);
        }
    },
    ;

    public abstract Mono<Member> get(DiscordCommandContext context);
}
