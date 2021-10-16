package com.github.kboyle.oktane.core.execution;

import com.github.kboyle.oktane.core.command.*;
import com.github.kboyle.oktane.core.mapping.CommandMap;
import com.github.kboyle.oktane.core.mapping.CommandMapProvider;
import com.github.kboyle.oktane.core.parsing.*;
import com.github.kboyle.oktane.core.precondition.ParameterPreconditionAnnotationConsumer;
import com.github.kboyle.oktane.core.precondition.PreconditionAnnotationConsumer;
import com.github.kboyle.oktane.core.prefix.PrefixSupplier;
import com.github.kboyle.oktane.core.result.Result;
import com.github.kboyle.oktane.core.result.argumentparser.ArgumentParserResult;
import com.github.kboyle.oktane.core.result.execution.*;
import com.github.kboyle.oktane.core.result.precondition.ParameterPreconditionResult;
import com.github.kboyle.oktane.core.result.precondition.ParameterPreconditionSuccessfulResult;
import com.google.common.base.Preconditions;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.kboyle.oktane.core.Utilities.Objects.getIfNull;
import static com.github.kboyle.oktane.core.Utilities.Objects.isBoxedPrimitive;

public class DefaultCommandService implements CommandService {
    private final List<CommandModule> modules;
    private final PrefixSupplier prefixSupplier;
    private final CommandMap commandMap;
    private final Tokeniser tokeniser;
    private final ArgumentParser argumentParser;
    private final TypeParserProvider typeParserProvider;

    protected DefaultCommandService(CommandService.Builder builder) {
        Preconditions.checkNotNull(builder, "builder cannot be null");

        this.modules = getModules(builder);
        this.prefixSupplier = getIfNull(builder.prefixSupplier(), PrefixSupplier::empty);
        this.commandMap = getIfNull(builder.commandMapProvider(), CommandMap::provider).create(modules);
        this.tokeniser = getIfNull(builder.tokeniser(), Tokeniser::get);
        this.argumentParser = getIfNull(builder.argumentParser(), ArgumentParser::get);
        this.typeParserProvider = builder.typeParserProvider();
    }

    private static List<CommandModule> getModules(CommandService.Builder builder) {
        var preBuiltModules = builder.commandModules().stream();
        var builtModules = builder.commandModuleBuilders().stream()
            .map(module -> module.build(null));
        var factoryModules = builder.commandModulesFactories().stream()
            .flatMap(factory -> factory.createModules(builder.typeParserProvider()));

        return Stream.of(preBuiltModules, builtModules, factoryModules)
            .flatMap(Function.identity())
            .toList();
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

            var argumentParserResult = argumentParser.parse(context, command, tokens);
            if (!tokeniserResult.success()) {
                failureResults.add(argumentParserResult);
                continue;
            }

            context.arguments = argumentParserResult.parsedArguments();
            var parameterPreconditionResult = runParameterPreconditions(context, command, argumentParserResult);
            if (!parameterPreconditionResult.success()) {
                failureResults.add(parameterPreconditionResult);
                continue;
            }

            context.dependencies = getDependencies(context, command.module().dependencies());

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
            "incompatible number of parameters (%d) to arguments (%d)",
            size,
            arguments.length
        );

        for (int i = 0; i < size; i++) {
            var parameter = parameters.get(i);
            var argument = arguments[i];

            var parameterPreconditionResult = parameter.varargs()
                ? runPreconditionsVarargs(context, parameter, argument)
                : runPreconditions(context, parameter, argument);

            if (!parameterPreconditionResult.success()) {
                return parameterPreconditionResult;
            }
        }

        return ParameterPreconditionSuccessfulResult.get();
    }

    private static <T> ParameterPreconditionResult<T> runPreconditionsVarargs(CommandContext context, CommandParameter<T> parameter, Object arguments) {
        // todo this is "slow", solve with code-gen?
        var length = Array.getLength(arguments);
        for (int i = 0; i < length; i++) {
            var argument = Array.get(arguments, i);
            var result = runPreconditions(context, parameter, argument);
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

    private Object[] getDependencies(CommandContext context, List<Class<?>> dependencyClasses) {
        if (dependencyClasses.isEmpty()) {
            return new Object[0];
        }

        var dependencies = new Object[dependencyClasses.size()];
        for (var i = 0; i < dependencyClasses.size(); i++) {
            var dependencyClass = dependencyClasses.get(i);
            if (dependencyClass.equals(getClass())) {
                dependencies[i] = this;
                continue;
            }

            dependencies[i] = Preconditions.checkNotNull(
                context.dependencyProvider().get(dependencyClass),
                "A dependency of type %s must be in your provider",
                dependencyClass
            );
        }

        return dependencies;
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

    protected static class Builder implements CommandService.Builder {
        final List<CommandModule.Builder> commandModuleBuilders;
        final List<CommandModule> commandModules;
        final List<CommandModulesFactory> commandModulesFactories;
        final List<PreconditionAnnotationConsumer<?>> preconditionAnnotationConsumers;
        final List<ParameterPreconditionAnnotationConsumer<?>> parameterPreconditionAnnotationConsumers;

        PrefixSupplier prefixSupplier;
        CommandMapProvider commandMapProvider;
        Tokeniser tokeniser;
        ArgumentParser argumentParser;
        TypeParserProvider typeParserProvider;

        protected Builder() {
            this.commandModuleBuilders = new ArrayList<>();
            this.commandModules = new ArrayList<>();
            this.commandModulesFactories = new ArrayList<>();
            this.preconditionAnnotationConsumers = new ArrayList<>();
            this.parameterPreconditionAnnotationConsumers = new ArrayList<>();
        }

        @Override
        public Builder prefixSupplier(PrefixSupplier prefixSupplier) {
            this.prefixSupplier = Preconditions.checkNotNull(prefixSupplier, "prefixSupplier cannot be null");
            return this;
        }

        @Override
        public Builder typeParserProvider(TypeParserProvider typeParserProvider) {
            this.typeParserProvider = Preconditions.checkNotNull(typeParserProvider, "typeParserProvider cannot be null");
            return this;
        }

        @Override
        public Builder preconditionConsumer(PreconditionAnnotationConsumer<?> preconditionConsumer) {
            Preconditions.checkNotNull(preconditionConsumer, "preconditionConsumer cannot be null");
            Preconditions.checkNotNull(
                preconditionConsumer.annotationClass(),
                "PreconditionConsumer#targetAnnotationType cannot return null"
            );
            preconditionAnnotationConsumers.add(preconditionConsumer);
            return this;
        }

        @Override
        public Builder parameterPreconditionConsumer(ParameterPreconditionAnnotationConsumer<?> parameterPreconditionConsumer) {
            Preconditions.checkNotNull(parameterPreconditionConsumer, "parameterPreconditionConsumer cannot be null");
            Preconditions.checkNotNull(
                parameterPreconditionConsumer.annotationClass(),
                "ParameterPreconditionConsumer#targetAnnotationType cannot return null"
            );
            parameterPreconditionAnnotationConsumers.add(parameterPreconditionConsumer);
            return this;
        }

        @Override
        public Builder module(CommandModule.Builder module) {
            commandModuleBuilders.add(Preconditions.checkNotNull(module, "module cannot be null"));
            return this;
        }

        @Override
        public Builder module(CommandModule module) {
            commandModules.add(Preconditions.checkNotNull(module, "module cannot be null"));
            return this;
        }

        @Override
        public Builder modulesFactory(CommandModulesFactory modulesFactory) {
            Preconditions.checkNotNull(modulesFactory, "modulesFactory cannot be null");
            commandModulesFactories.add(modulesFactory);
            return this;
        }

        @Override
        public Builder commandMapProvider(CommandMapProvider mapProvider) {
            this.commandMapProvider = Preconditions.checkNotNull(mapProvider, "mapProvider cannot be null");
            return this;
        }

        @Override
        public Builder tokeniser(Tokeniser tokeniser) {
            this.tokeniser = Preconditions.checkNotNull(tokeniser, "tokeniser cannot be null");
            return this;
        }

        @Override
        public Builder argumentParser(ArgumentParser argumentParser) {
            this.argumentParser = Preconditions.checkNotNull(argumentParser, "argumentParser cannot be null");
            return this;
        }

        @Override
        public PrefixSupplier prefixSupplier() {
            return prefixSupplier;
        }

        @Override
        public List<ParameterPreconditionAnnotationConsumer<?>> parameterPreconditionAnnotationConsumers() {
            return null;
        }

        @Override
        public List<PreconditionAnnotationConsumer<?>> preconditionAnnotationConsumers() {
            return null;
        }

        @Override
        public List<CommandModule.Builder> commandModuleBuilders() {
            return commandModuleBuilders;
        }

        @Override
        public List<CommandModule> commandModules() {
            return commandModules;
        }

        @Override
        public List<CommandModulesFactory> commandModulesFactories() {
            return commandModulesFactories;
        }

        @Override
        public CommandMapProvider commandMapProvider() {
            return commandMapProvider;
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

        @Override
        public DefaultCommandService build() {
            return new DefaultCommandService(this);
        }
    }
}
