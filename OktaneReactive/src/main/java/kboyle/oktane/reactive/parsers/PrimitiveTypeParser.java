package kboyle.oktane.reactive.parsers;

import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.results.typeparser.TypeParserResult;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class PrimitiveTypeParser<T> implements ReactiveTypeParser<T> {
    private final Class<T> cl;
    private final Function<String, T> parseFunc;

    public PrimitiveTypeParser(Class<T> cl, Function<String, T> parseFunc) {
        this.cl = cl;
        this.parseFunc = parseFunc;
    }

    @Override
    public Mono<TypeParserResult<T>> parse(CommandContext context, ReactiveCommand command, String input) {
        try {
            return monoSuccess(parseFunc.apply(input));
        } catch (Exception ex) {
            return monoFailure("Failed to parse %s as %s", input, cl);
        }
    }
}
