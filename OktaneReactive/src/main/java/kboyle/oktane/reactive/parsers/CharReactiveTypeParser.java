package kboyle.oktane.reactive.parsers;

import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.results.typeparser.TypeParserResult;
import reactor.core.publisher.Mono;

public class CharReactiveTypeParser implements ReactiveTypeParser<Character> {
    @Override
    public Mono<TypeParserResult<Character>> parse(CommandContext context, ReactiveCommand command, String input) {
        if (input.length() != 1) {
            return monoFailure("A char can only be a single character");
        }
        return monoSuccess(input.charAt(0));
    }
}
