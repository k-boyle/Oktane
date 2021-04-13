package kboyle.oktane.reactive.parsers;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.module.ReactiveCommandParameter;
import kboyle.oktane.reactive.results.Result;
import kboyle.oktane.reactive.results.argumentparser.ArgumentParserFailedResult;
import kboyle.oktane.reactive.results.argumentparser.ArgumentParserResult;
import kboyle.oktane.reactive.results.argumentparser.ArgumentParserSuccessfulResult;
import kboyle.oktane.reactive.results.typeparser.TypeParserResult;
import kboyle.oktane.reactive.results.typeparser.TypeParserSuccessfulResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

public class DefaultReactiveArgumentParser implements ReactiveArgumentParser {
    private final ImmutableMap<Class<?>, ReactiveTypeParser<?>> typeParserByClass;

    public DefaultReactiveArgumentParser(ImmutableMap<Class<?>, ReactiveTypeParser<?>> typeParserByClass) {
        this.typeParserByClass = typeParserByClass;
    }

    @Override
    public Mono<ArgumentParserResult> parse(CommandContext context, ReactiveCommand command, List<String> tokens) {
        ImmutableList<ReactiveCommandParameter> parameters = command.parameters();

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

    private Mono<Result> parse(ReactiveCommandParameter parameter, CommandContext context, String input) {
        Class<?> type = parameter.type();

        if (type == String.class) {
            return Mono.just(new TypeParserSuccessfulResult<>(input));
        }

        ReactiveTypeParser<?> parser = parameter.parser();
        if (parser == null) {
            parser = Preconditions.checkNotNull(
                typeParserByClass.get(type),
                "Missing type parser for type %s",
                type
            );
        }

        return parser.parse(context, parameter.command(), input).cast(Result.class);
    }
}
