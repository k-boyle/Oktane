package kboyle.oktane.core.parsers;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.results.typeparser.TypeParserFailedResult;
import kboyle.oktane.core.results.typeparser.TypeParserResult;
import kboyle.oktane.core.results.typeparser.TypeParserSuccessfulResult;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface TypeParser<T> {
    Mono<TypeParserResult<T>> parse(CommandContext context, Command command, String input);

    default TypeParserResult<T> success(T value) {
        return new TypeParserSuccessfulResult<>(value);
    }

    default TypeParserResult<T> failure(String reason, Object... args) {
        return new TypeParserFailedResult<>(String.format(reason, args));
    }
}
