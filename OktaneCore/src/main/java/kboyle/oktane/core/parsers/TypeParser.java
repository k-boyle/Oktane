package kboyle.oktane.core.parsers;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.results.typeparser.TypeParserFailedResult;
import kboyle.oktane.core.results.typeparser.TypeParserResult;
import kboyle.oktane.core.results.typeparser.TypeParserSuccessfulResult;

@FunctionalInterface
public interface TypeParser<T> {
    TypeParserResult parse(CommandContext context, String input);

    default TypeParserSuccessfulResult<T> success(T value) {
        return new TypeParserSuccessfulResult<>(value);
    }

    default TypeParserFailedResult failure(String reason, Object... args) {
        return new TypeParserFailedResult(String.format(reason, args));
    }
}
