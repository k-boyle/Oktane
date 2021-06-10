package kboyle.oktane.core.parsers;

import com.google.common.base.Defaults;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.CommandParameter;
import kboyle.oktane.core.results.argumentparser.ArgumentParserFailedResult;
import kboyle.oktane.core.results.argumentparser.ArgumentParserResult;
import kboyle.oktane.core.results.argumentparser.ArgumentParserSuccessfulResult;
import kboyle.oktane.core.results.typeparser.TypeParserResult;
import kboyle.oktane.core.results.typeparser.TypeParserSuccessfulResult;
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

        return parse(context, command, 0, tokens, parameters, new Object[parameters.size()]);
    }

    private Mono<ArgumentParserResult> parse(
            CommandContext context,
            Command command,
            int index,
            List<String> tokens,
            ImmutableList<CommandParameter> parameters,
            Object[] parsedArguments) {
        if (index == parameters.size()) {
            return new ArgumentParserSuccessfulResult(command, parsedArguments).mono();
        }

        var parameter = parameters.get(index);
        var token = getNextToken(tokens, index);

        if (token == null) {
            Preconditions.checkState(parameter.optional, "A non-optional parameter with a null token should not be possible");
            token = parameter.defaultValue.orElse(null);
        }

        if (token == null) {
            parsedArguments[index] = Defaults.defaultValue(parameter.type);
            return parse(context, command, index + 1, tokens, parameters, parsedArguments);
        }

        var tokenCopy = token;
        return parse(parameter, context, token)
            .flatMap(result -> {
                if (!result.success()) {
                    return new ArgumentParserFailedResult(parameter, tokenCopy, result).mono();
                }

                parsedArguments[index] = result.value();
                return parse(context, command, index + 1, tokens, parameters, parsedArguments);
            });
    }

    private String getNextToken(List<String> tokens, int index) {
        if (index >= tokens.size()) {
            return null;
        }

        return tokens.get(index);
    }

    private Mono<? extends TypeParserResult<?>> parse(CommandParameter parameter, CommandContext context, String token) {
        var type = parameter.type;

        if (type == String.class) {
            return Mono.just(new TypeParserSuccessfulResult<>(token));
        }

        var parser = parameter.parser;
        if (parser == null) {
            parser = Preconditions.checkNotNull(
                typeParserByClass.get(type),
                "Missing type parser for type %s",
                type
            );
        }

        return parser.parse(context, parameter.command, token);
    }
}
