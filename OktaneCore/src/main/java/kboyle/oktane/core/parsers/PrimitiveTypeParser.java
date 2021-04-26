package kboyle.oktane.core.parsers;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.results.typeparser.TypeParserResult;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class PrimitiveTypeParser<T> implements TypeParser<T> {
    private final Class<T> cl;
    private final Function<String, T> parseFunc;

    public PrimitiveTypeParser(Class<T> cl, Function<String, T> parseFunc) {
        this.cl = cl;
        this.parseFunc = parseFunc;
    }

    @Override
    public Mono<TypeParserResult<T>> parse(CommandContext context, Command command, String input) {
        return parse(input).mono();
    }

    private TypeParserResult<T> parse(String input) {
        try {
            return success(parseFunc.apply(input));
        } catch (Exception ex) {
            return failure("Failed to parse %s as %s", input, cl);
        }
    }
}
