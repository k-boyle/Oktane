package kboyle.oktane.reactive.parsers;

import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.results.typeparser.TypeParserFailedResult;
import kboyle.oktane.reactive.results.typeparser.TypeParserResult;
import kboyle.oktane.reactive.results.typeparser.TypeParserSuccessfulResult;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface ReactiveTypeParser<T> {
    Mono<TypeParserResult<T>> parse(CommandContext context, ReactiveCommand command, String input);

    default TypeParserResult<T> success(T value) {
        return new TypeParserSuccessfulResult<>(value);
    }

    default TypeParserResult<T> failure(String reason, Object... args) {
        return new TypeParserFailedResult<>(String.format(reason, args));
    }
}
