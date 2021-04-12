package kboyle.oktane.reactive.parsers;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.mapping.CommandMatch;
import kboyle.oktane.reactive.module.Command;
import kboyle.oktane.reactive.module.CommandParameter;
import kboyle.oktane.reactive.results.Result;
import kboyle.oktane.reactive.results.argumentparser.*;
import kboyle.oktane.reactive.results.tokeniser.TokeniserResult;
import kboyle.oktane.reactive.results.typeparser.TypeParserResult;
import kboyle.oktane.reactive.results.typeparser.TypeParserSuccessfulResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

public class DefaultArgumentParser {
//    private static final Mono<ArgumentParserResult> EMPTY_SUCCESS = Mono.just(ArgumentParserSuccessfulResult.empty());

    private static final char SPACE = ' ';
    private static final char QUOTE = '"';
    private static final char ESCAPE = '\\';
    private static final String EMPTY = "";

    private final ImmutableMap<Class<?>, TypeParser<?>> typeParserByClass;
    private final Tokeniser tokeniser = new Tokeniser();

    public DefaultArgumentParser(ImmutableMap<Class<?>, TypeParser<?>> typeParserByClass) {
        this.typeParserByClass = typeParserByClass;
    }

    public Mono<ArgumentParserResult> parse(CommandContext context, CommandMatch commandMatch, String input) {
        Command command = commandMatch.command();
        int index = commandMatch.argumentStart();
//        int commandEnd = commandMatch.commandEnd();
//        int inputLength = input.length();
//        int inputLastIndex = inputLength - 1;
        ImmutableList<CommandParameter> parameters = command.parameters();
//
//        // todo yeet
//        boolean emptyParameters = parameters.isEmpty();
//        if ((index == commandEnd || inputLastIndex == commandEnd) && !emptyParameters) {
//            return Mono.just(new ArgumentParserFailedResult(command, ParserFailedReason.TOO_FEW_ARGUMENTS, index));
//        }
//
//        if (emptyParameters) {
//            if (commandEnd != index && noneWhitespaceRemains(input, index)) {
//                return Mono.just(new ArgumentParserFailedResult(command, ParserFailedReason.TOO_MANY_ARGUMENTS, index));
//            }
//
//            return EMPTY_SUCCESS;
//        }

        // don't tokenise in here, tokenise prior to calling this, saves wrapping results
        TokeniserResult tokeniserResult = tokeniser.tokenise(input, commandMatch);

        if (!tokeniserResult.success()) {
            return Mono.just(new ArgumentParserTokenisationFailedResult(tokeniserResult));
        }

        List<String> tokens = tokeniserResult.tokens();

        return Flux.fromIterable(tokens)
            .zipWithIterable(parameters, (token, parameter) -> parse(parameter, context, input))
            .flatMap(Function.identity())
            .collectList()
            .map(results -> {
                boolean allSuccess = results.stream().allMatch(Result::success);

                if (!allSuccess) {
                    return new ArgumentParserFailedResult(command, null, index);
                }

                Object[] arguments = results.stream()
                    .map(TypeParserResult.class::cast)
                    .map(TypeParserResult::value)
                    .toArray();

                return new ArgumentParserSuccessfulResult(command, arguments);
            });
    }

    private Mono<Result> parse(CommandParameter parameter, CommandContext context, String input) {
        // todo
        if (parameter.type() == String.class) {
            return Mono.just(new TypeParserSuccessfulResult<>(input));
        }

        TypeParser<?> parser = parameter.parser();
        if (parser == null) {
            parser = Preconditions.checkNotNull(
                typeParserByClass.get(parameter.type()),
                "Missing type parser for type %s",
                parameter.type()
            );
        }

        return parser.parse(context, parameter.command(), input)
            .cast(Result.class)
            .onErrorResume(ex -> Mono.just(new ArgumentParserExceptionResult(parameter.command(), ex)));
    }
}
