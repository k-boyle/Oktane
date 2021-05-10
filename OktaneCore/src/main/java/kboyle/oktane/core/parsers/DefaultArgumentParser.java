package kboyle.oktane.core.parsers;

import com.google.common.base.Defaults;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.CommandParameter;
import kboyle.oktane.core.results.Result;
import kboyle.oktane.core.results.argumentparser.ArgumentParserFailedResult;
import kboyle.oktane.core.results.argumentparser.ArgumentParserResult;
import kboyle.oktane.core.results.argumentparser.ArgumentParserSuccessfulResult;
import kboyle.oktane.core.results.typeparser.TypeParserResult;
import kboyle.oktane.core.results.typeparser.TypeParserSuccessfulResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class DefaultArgumentParser implements ArgumentParser {
    private static final Object[] EMPTY = new Object[0];

    private final ImmutableMap<Class<?>, TypeParser<?>> typeParserByClass;

    public DefaultArgumentParser(ImmutableMap<Class<?>, TypeParser<?>> typeParserByClass) {
        this.typeParserByClass = typeParserByClass;
    }

    @Override
    public Mono<ArgumentParserResult> parse(CommandContext context, Command command, List<String> tokens) {
        var parameters = command.parameters;

        if (parameters.isEmpty()) {
            return Mono.just(new ArgumentParserSuccessfulResult(command, EMPTY));
        }

        return Flux.range(0, parameters.size())
            .flatMap(i -> {
                var parameter = parameters.get(i);
                var token = getNextToken(tokens, i);

                if (token == null) {
                    Preconditions.checkState(parameter.optional, "A non-optional parameter with a null token should not be possible");
                    token = parameter.defaultValue.orElse(null);
                }

                if (token == null) {
                    return new TypeParserSuccessfulResult<>(Defaults.defaultValue(parameter.type)).mono();
                }

                return parse(parameter, context, token);
            })
            .collectList()
            .map(results -> {
                var allSuccess = results.stream().allMatch(Result::success);

                if (!allSuccess) {
                    return new ArgumentParserFailedResult(command, results);
                }

                var arguments = results.stream()
                    .map(TypeParserResult.class::cast)
                    .map(TypeParserResult::value)
                    .toArray();

                return new ArgumentParserSuccessfulResult(command, arguments);
            });
    }

    private String getNextToken(List<String> tokens, int index) {
        if (index >= tokens.size()) {
            return null;
        }

        return tokens.get(index);
    }

    private Mono<Result> parse(CommandParameter parameter, CommandContext context, String input) {
        var type = parameter.type;

        if (type == String.class) {
            return Mono.just(new TypeParserSuccessfulResult<>(input));
        }

        var parser = parameter.parser;
        if (parser == null) {
            parser = Preconditions.checkNotNull(
                typeParserByClass.get(type),
                "Missing type parser for type %s",
                type
            );
        }

        return parser.parse(context, parameter.command, input).cast(Result.class);
    }
}
