package com.github.kboyle.oktane.core.execution;

import com.github.kboyle.oktane.core.command.*;
import com.github.kboyle.oktane.core.configuration.OktaneConfiguration;
import com.github.kboyle.oktane.core.mapping.CommandMap;
import com.github.kboyle.oktane.core.mapping.CommandMapProvider;
import com.github.kboyle.oktane.core.parsing.*;
import com.github.kboyle.oktane.core.prefix.PrefixSupplier;
import com.github.kboyle.oktane.core.result.Result;
import com.github.kboyle.oktane.core.result.argumentparser.ArgumentParserResult;
import com.github.kboyle.oktane.core.result.execution.*;
import com.github.kboyle.oktane.core.result.precondition.ParameterPreconditionResult;
import com.github.kboyle.oktane.core.result.precondition.ParameterPreconditionSuccessfulResult;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.kboyle.oktane.core.Utilities.Objects.isBoxedPrimitive;

@Component
@Import(OktaneConfiguration.class)
public class DefaultCommandService implements CommandService {
    private final List<CommandModule> modules;
    private final PrefixSupplier prefixSupplier;
    private final CommandMap commandMap;
    private final Tokeniser tokeniser;
    private final ArgumentParser argumentParser;
    private final TypeParserProvider typeParserProvider;

    @Autowired
    public DefaultCommandService(
            List<CommandModule.Builder> commandModuleBuilders,
            List<CommandModulesFactory> commandModulesFactories,
            PrefixSupplier prefixSupplier,
            CommandMapProvider commandMapProvider,
            Tokeniser tokeniser,
            ArgumentParser argumentParser,
            TypeParserProvider typeParserProvider) {

        Preconditions.checkNotNull(commandModuleBuilders, "commandModuleBuilders cannot be null");
        Preconditions.checkNotNull(commandModulesFactories, "commandModulesFactories cannot be null");
        Preconditions.checkNotNull(prefixSupplier, "prefixSupplier cannot be null");
        Preconditions.checkNotNull(commandMapProvider, "commandMapProvider cannot be null");
        Preconditions.checkNotNull(tokeniser, "tokeniser cannot be null");
        Preconditions.checkNotNull(argumentParser, "argumentParser cannot be null");
        Preconditions.checkNotNull(typeParserProvider, "typeParserProvider cannot be null");

        var builtModules = commandModuleBuilders.stream()
            .map(module -> module.build(null));
        var factoryModules = commandModulesFactories.stream()
            .flatMap(factory -> factory.createModules(typeParserProvider));

        this.modules = Stream.of(builtModules, factoryModules)
            .flatMap(Function.identity())
            .toList();

        this.prefixSupplier = prefixSupplier;
        this.commandMap = Preconditions.checkNotNull(commandMapProvider.create(this.modules), "CommandMapProvider#create cannot return null");
        this.tokeniser = tokeniser;
        this.argumentParser = argumentParser;
        this.typeParserProvider = typeParserProvider;
    }

    public DefaultCommandService(Properties properties) {
        this(
            List.copyOf(properties.commandModuleBuilders),
            List.copyOf(properties.commandModulesFactories),
            properties.prefixSupplier,
            properties.commandMapProvider,
            properties.tokeniser,
            properties.argumentParser,
            properties.typeParserProvider
        );
    }

    @Override
    public Result execute(CommandContext context, String input, int startIndex) {
        Preconditions.checkNotNull(context, "context cannot be null");
        Preconditions.checkNotNull(input, "input cannot be null");

        if (input.isEmpty()) {
            return CommandNotFoundResult.get();
        }

        Preconditions.checkState(startIndex >= 0, "startIndex must be positive");
        Preconditions.checkState(startIndex < input.length(), "startIndex must be less than the input length");

        startIndex = getStartIndex(context, input, startIndex);
        if (startIndex == -1) {
            return new MissingPrefixResult(input, startIndex);
        }

        var matches = commandMap.findMatches(input, startIndex);
        if (matches.isEmpty()) {
            return CommandNotFoundResult.get();
        }

        var failureResults = new ArrayList<Result>();
        for (var match : matches) {
            var command = match.command();
            context.command = command;

            var preconditionResult = command.runPreconditions(context);
            if (!preconditionResult.success()) {
                failureResults.add(preconditionResult);
                continue;
            }

            var tokeniserResult = tokeniser.tokenise(input, match);
            if (!tokeniserResult.success()) {
                failureResults.add(tokeniserResult);
                continue;
            }

            var tokens = tokeniserResult.tokens();
            context.tokens = tokens;

            var argumentParserResult = argumentParser.parse(context);
            if (!argumentParserResult.success()) {
                failureResults.add(argumentParserResult);
                continue;
            }

            context.arguments = argumentParserResult.parsedArguments();
            var parameterPreconditionResult = runParameterPreconditions(context, command, argumentParserResult);
            if (!parameterPreconditionResult.success()) {
                failureResults.add(parameterPreconditionResult);
                continue;
            }

            command.module().before().run();
            var commandResult = command.callback().execute(context);
            command.module().after().run();

            return commandResult;
        }

        return new CommandMatchFailResult(failureResults);
    }

    protected int getStartIndex(CommandContext context, String input, int startIndex) {
        var prefixes = Preconditions.checkNotNull(prefixSupplier.get(context), "PrefixSupplier#get cannot return null");
        if (prefixes.isEmpty()) {
            return startIndex;
        }

        for (var prefix : prefixes) {
            Preconditions.checkNotNull(prefix, "prefix cannot be null");

            var index = prefix.startsWith(context, input, startIndex);
            if (index != -1) {
                context.prefix = prefix;
                return index;
            }
        }

        return -1;
    }

    private ParameterPreconditionResult<?> runParameterPreconditions(
            CommandContext context,
            Command command,
            ArgumentParserResult argumentParserResult) {

        var parameters = command.parameters();
        var arguments = argumentParserResult.parsedArguments();
        var size = parameters.size();

        Preconditions.checkState(
            size == arguments.length,
            "incompatible number of parameters (%s) to arguments (%s)",
            size,
            arguments.length
        );

        for (int i = 0; i < size; i++) {
            var parameter = parameters.get(i);
            var argument = arguments[i];

            var parameterPreconditionResult = parameter.greedy()
                    ? runPreconditionsGreedy(context, parameter, argument)
                    : runPreconditions(context, parameter, argument);

            if (!parameterPreconditionResult.success()) {
                return parameterPreconditionResult;
            }
        }

        return ParameterPreconditionSuccessfulResult.get();
    }

    @SuppressWarnings("unchecked")
    private static <T> ParameterPreconditionResult<T> runPreconditionsGreedy(CommandContext context, CommandParameter<T> parameter, Object argument) {
        var greedyList = (List<T>) argument;
        for (var value : greedyList) {
            var result = runPreconditions(context, parameter, value);
            if (!result.success()) {
                return result;
            }
        }

        return ParameterPreconditionSuccessfulResult.get();
    }

    @SuppressWarnings("unchecked")
    private static <T> ParameterPreconditionResult<T> runPreconditions(CommandContext context, CommandParameter<T> parameter, Object argument) {
        var type = Preconditions.checkNotNull(parameter.type(), "CommandParameter#type cannot return null");
        if (!type.isPrimitive()) {
            Preconditions.checkState(argument == null || type.isInstance(argument), "argument is of type %s expected %s", argument == null ? "null" : argument.getClass(), type);
            return parameter.runPreconditions(context, type.cast(argument));
        }

        Preconditions.checkState(isBoxedPrimitive(type, argument), "argument is of type %s expected %s", argument.getClass(), type);
        return parameter.runPreconditions(context, (T) argument);
    }

    @Override
    public List<CommandModule> modules() {
        return modules;
    }

    @Override
    public PrefixSupplier prefixSupplier() {
        return prefixSupplier;
    }

    @Override
    public CommandMap commandMap() {
        return commandMap;
    }

    @Override
    public Tokeniser tokeniser() {
        return tokeniser;
    }

    @Override
    public ArgumentParser argumentParser() {
        return argumentParser;
    }

    @Override
    public TypeParserProvider typeParserProvider() {
        return typeParserProvider;
    }

    public static class Properties {
        public List<CommandModule.Builder> commandModuleBuilders = new ArrayList<>();
        public List<CommandModulesFactory> commandModulesFactories = new ArrayList<>();
        public PrefixSupplier prefixSupplier = PrefixSupplier.empty();
        public CommandMapProvider commandMapProvider = CommandMap.provider();
        public Tokeniser tokeniser = Tokeniser.get();
        public ArgumentParser argumentParser = ArgumentParser.get();
        public TypeParserProvider typeParserProvider = TypeParserProvider.defaults();
    }
}
