package kboyle.oktane.core.parsers;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
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
import java.util.function.Function;

public class DefaultArgumentParser implements ArgumentParser {
    private static final Object[] EMPTY = new Object[0];

    private final ImmutableMap<Class<?>, TypeParser<?>> typeParserByClass;

    public DefaultArgumentParser(ImmutableMap<Class<?>, TypeParser<?>> typeParserByClass) {
        this.typeParserByClass = typeParserByClass;
    }

    @Override
    public Mono<ArgumentParserResult> parse(CommandContext context, Command command, List<String> tokens) {
        ImmutableList<CommandParameter> parameters = command.parameters;

        if (parameters.isEmpty()) {
            return Mono.just(new ArgumentParserSuccessfulResult(command, EMPTY));
        }

        return Flux.fromIterable(tokens)
            .zipWithIterable(parameters, (token, parameter) -> parse(parameter, context, token))
            .flatMap(Function.identity())
            .collectList()
            .map(results -> {
                boolean allSuccess = results.stream().allMatch(Result::success);

                if (!allSuccess) {
                    return new ArgumentParserFailedResult(command, results);
                }

                Object[] arguments = results.stream()
                    .map(TypeParserResult.class::cast)
                    .map(TypeParserResult::value)
                    .toArray();

                return new ArgumentParserSuccessfulResult(command, arguments);
            });
    }

    private Mono<Result> parse(CommandParameter parameter, CommandContext context, String input) {
        Class<?> type = parameter.type;

        if (type == String.class) {
            return Mono.just(new TypeParserSuccessfulResult<>(input));
        }

        TypeParser<?> parser = parameter.parser;
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
