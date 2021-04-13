package kboyle.oktane.reactivetest;

import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.parsers.ReactiveTypeParser;
import kboyle.oktane.reactive.results.typeparser.TypeParserResult;
import reactor.core.publisher.Mono;

public class ThrowingTypeParser implements ReactiveTypeParser<Exception> {
    @Override
    public Mono<TypeParserResult<Exception>> parse(CommandContext context, ReactiveCommand command, String input) {
        throw new RuntimeException();
    }
}
