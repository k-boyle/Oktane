package kboyle.oktane.core;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.ClassPath;
import kboyle.oktane.core.exceptions.RuntimeIOException;
import kboyle.oktane.core.mapping.CommandMap;
import kboyle.oktane.core.mapping.CommandMatch;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.CommandModule;
import kboyle.oktane.core.module.ModuleBase;
import kboyle.oktane.core.module.factory.CommandModuleFactory;
import kboyle.oktane.core.parsers.*;
import kboyle.oktane.core.results.Result;
import kboyle.oktane.core.results.argumentparser.ArgumentParserSuccessfulResult;
import kboyle.oktane.core.results.search.CommandMatchFailedResult;
import kboyle.oktane.core.results.search.CommandNotFoundResult;
import kboyle.oktane.core.results.tokeniser.TokeniserSuccessfulResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static kboyle.oktane.core.module.CommandUtils.isValidModuleClass;

/**
 * The entry point for executing commands.
 *
 * @param <T> The type of context that's used in commands.
 */
public class CommandHandler<T extends CommandContext> {
    private static final Mono<Result> COMMAND_NOT_FOUND = Mono.just(CommandNotFoundResult.get());
    private static final Object[] EMPTY_BEANS = new Object[0];

    private final CommandMap commandMap;
    private final ArgumentParser argumentParser;
    private final Tokeniser tokeniser;
    private final ImmutableList<CommandModule> modules;

    private CommandHandler(
            CommandMap commandMap,
            ArgumentParser argumentParser,
            Tokeniser tokeniser,
            ImmutableList<CommandModule> modules) {
        this.commandMap = commandMap;
        this.argumentParser = argumentParser;
        this.tokeniser = tokeniser;
        this.modules = modules;
    }

    /**
     * Creates a new builder for the CommandHandler.
     *
     * @return A new CommandHandler builder.
     */
    public static <T extends CommandContext> Builder<T> builder() {
        return new Builder<>();
    }

    public Mono<Result> execute(String input, T context) {
        Preconditions.checkNotNull(input);
        Preconditions.checkNotNull(context);

        if (input.isEmpty()) {
            return COMMAND_NOT_FOUND;
        }

        ImmutableList<CommandMatch> matches = commandMap.findCommands(input);

        if (matches.isEmpty()) {
            return COMMAND_NOT_FOUND;
        }

        return Flux.fromIterable(matches)
            .flatMap(match -> match.command().runPreconditions(context)
                .map(result -> {
                    if (!result.success()) {
                        return result;
                    }

                    return tokeniser.tokenise(input, match);
                })
            )
            .flatMap(result0 -> {
                if (!result0.success()) {
                    return Mono.just(result0);
                }

                TokeniserSuccessfulResult result = (TokeniserSuccessfulResult) result0;
                return argumentParser.parse(context, result.command(), result.tokens());
            })
            .collectList()
            .flatMap(results -> results.stream()
                .filter(Result::success)
                .map(ArgumentParserSuccessfulResult.class::cast)
                .findFirst()
                .map(result -> executeCommand(context, result))
                .orElseGet(() -> Mono.just(new CommandMatchFailedResult(results)))
            );
    }

    private Mono<Result> executeCommand(T context, ArgumentParserSuccessfulResult parserResult) {
        Command command = parserResult.command();
        context.command = command;
        Object[] beans = getBeans(context, command.module.beans);
        return command.commandCallback
            .execute(context, beans, parserResult.parsedArguments())
            .cast(Result.class);
    }

    /**
     * @return All of the modules that belong to the CommandHandler.
     */
    public ImmutableList<CommandModule> modules() {
        return modules;
    }

    /**
     * @return All of the commands that belong to the CommandHandler.
     */
    public Stream<Command> commands() {
        return modules.stream().flatMap(module -> module.commands.stream());
    }

    private Object[] getBeans(CommandContext context, ImmutableList<Class<?>> beanClazzes) {
        if (beanClazzes.isEmpty()) {
            return EMPTY_BEANS;
        }

        Object[] beans = new Object[beanClazzes.size()];
        for (int i = 0; i < beanClazzes.size(); i++) {
            Class<?> beanClazz = beanClazzes.get(i);
            if (beanClazz.equals(getClass())) {
                beans[i] = this;
                continue;
            }

            beans[i] = Preconditions.checkNotNull(
                context.beanProvider().getBean(beanClazz),
                "A bean of type %s must be in your provider",
                beanClazz
            );
        }

        return beans;
    }

    /**
     * A builder for the CommandHandler.
     *
     * @param <T> The type of context that's used in commands.
     */
    public static class Builder<T extends CommandContext> {
        private final Map<Class<?>, TypeParser<?>> typeParserByClass;
        private final CommandMap.Builder commandMap;
        private final List<Class<? extends ModuleBase<T>>> commandModules;

        private BeanProvider beanProvider;
        private ArgumentParser argumentParser;
        private Tokeniser tokeniser;

        private Builder() {
            this.typeParserByClass = new HashMap<>(PrimitiveTypeParserFactory.create());
            this.commandMap = CommandMap.builder();
            this.commandModules = new ArrayList<>();
            this.beanProvider = BeanProvider.empty();
            this.tokeniser = new DefaultTokeniser();
        }

        /**
         * Adds a type parser that will be used by the CommandHandler.
         *
         * @param clazz  The class representing the type that the TypeParser is for.
         * @param parser The TypeParser.
         * @param <S>    The type that the TypeParser is for.
         * @return The builder.
         */
        public <S> Builder<T> withTypeParser(Class<S> clazz, TypeParser<S> parser) {
            Preconditions.checkNotNull(clazz, "Clazz cannot be null");
            Preconditions.checkNotNull(parser, "Parser cannot be null");
            this.typeParserByClass.put(clazz, parser);
            return this;
        }

        /**
         * Adds a module that will be used by the CommandBuilder.
         *
         * @param moduleClazz The class representing the type of module that you want to add.
         * @param <S>         The type of module you want to add.
         * @return The builder.
         */
        public <S extends ModuleBase<T>> Builder<T> withModule(Class<S> moduleClazz) {
            Preconditions.checkNotNull(moduleClazz, "moduleClazz cannot be null");
            this.commandModules.add(moduleClazz);
            return this;
        }

        // todo fix
        /**
         * Adds all the modules in the same package as the contextClazz.
         *
         * @param contextClazz The class representing the type of context used for the CommandHandler.
         * @return The builder.
         */
        public Builder<T> withModules(Class<T> contextClazz) {
            return withModules(contextClazz, contextClazz.getPackageName());
        }

        /**
         * Adds all the modules in the specified package.
         *
         * @param contextClazz The class representing the type of context used for the CommandHandler.
         * @param packageName  The package name to search in.
         * @return The builder.
         */
        @SuppressWarnings("UnstableApiUsage")
        public Builder<T> withModules(Class<T> contextClazz, String packageName) {
            try {
                ClassPath.from(contextClazz.getClassLoader()).getTopLevelClassesRecursive(packageName)
                    .stream()
                    .map(ClassPath.ClassInfo::load)
                    .filter(clazz -> isValidModuleClass(contextClazz, clazz))
                    .forEach(clazz -> withModule(clazz.asSubclass(ModuleBase.class)));

                return this;
            } catch (IOException exception) {
                throw new RuntimeIOException(exception);
            }
        }

        /**
         * Adds the bean provider that will be used to fetch dependencies for singleton modules and preconditions.
         *
         * @param beanProvider The bean provider.
         * @return The builder.
         */
        public Builder<T> withBeanProvider(BeanProvider beanProvider) {
            Preconditions.checkNotNull(beanProvider, "beanProvider cannot be null");
            this.beanProvider = beanProvider;
            return this;
        }

        /**
         * Sets the ArgumentParser that will be used, if none is specified then the default will be used.
         *
         * @param argumentParser The argument parser.
         * @return The builder.
         */
        public Builder<T> withArgumentParser(ArgumentParser argumentParser) {
            Preconditions.checkNotNull(argumentParser, "argumentParser cannot be null");
            this.argumentParser = argumentParser;
            return this;
        }

        public Builder<T> withTokeniser(Tokeniser tokeniser) {
            Preconditions.checkNotNull(tokeniser, "tokeniser cannot be null");
            this.tokeniser = tokeniser;
            return this;
        }

        /**
         * Builds the CommandHandler.
         *
         * @return The built CommandHandler.
         */
        public CommandHandler<T> build() {
            List<CommandModule> modules = new ArrayList<>();
            CommandModuleFactory<T, ModuleBase<T>> moduleFactory = new CommandModuleFactory<>(
                beanProvider,
                typeParserByClass
            );

            for (Class<? extends ModuleBase<T>> moduleClazz : commandModules) {
                CommandModule module = moduleFactory.create(moduleClazz);
                modules.add(module);
                commandMap.map(module);
            }

            if (argumentParser == null) {
                argumentParser = new DefaultArgumentParser(ImmutableMap.copyOf(typeParserByClass));
            }

            return new CommandHandler<>(commandMap.build(), argumentParser, tokeniser, ImmutableList.copyOf(modules));
        }
    }
}
