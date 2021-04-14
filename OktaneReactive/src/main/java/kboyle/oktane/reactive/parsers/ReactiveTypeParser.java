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

    default Mono<TypeParserResult<T>> monoSuccess(T value) {
        return Mono.just(new TypeParserSuccessfulResult<>(value));
    }

    default TypeParserSuccessfulResult<T> success(T value) {
        return new TypeParserSuccessfulResult<>(value);
    }

    default Mono<TypeParserResult<T>> monoFailure(String reason, Object... args) {
        return Mono.just(new TypeParserFailedResult<>(String.format(reason, args)));
    }

    default TypeParserFailedResult<T> failure(String reason, Object... args) {
        return new TypeParserFailedResult<>(String.format(reason, args));
    }
}
