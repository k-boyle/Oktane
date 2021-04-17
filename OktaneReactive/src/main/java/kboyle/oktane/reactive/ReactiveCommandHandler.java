package kboyle.oktane.reactive;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.ClassPath;
import kboyle.oktane.reactive.exceptions.RuntimeIOException;
import kboyle.oktane.reactive.mapping.CommandMap;
import kboyle.oktane.reactive.mapping.CommandMatch;
import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.module.ReactiveModule;
import kboyle.oktane.reactive.module.ReactiveModuleBase;
import kboyle.oktane.reactive.module.factory.CommandModuleFactory;
import kboyle.oktane.reactive.parsers.*;
import kboyle.oktane.reactive.results.Result;
import kboyle.oktane.reactive.results.argumentparser.ArgumentParserSuccessfulResult;
import kboyle.oktane.reactive.results.search.CommandMatchFailedResult;
import kboyle.oktane.reactive.results.search.CommandNotFoundResult;
import kboyle.oktane.reactive.results.tokeniser.TokeniserSuccessfulResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static kboyle.oktane.reactive.module.CommandUtil.isValidModuleClass;

/**
 * The entry point for executing commands.
 *
 * @param <T> The type of context that's used in commands.
 */
public class ReactiveCommandHandler<T extends CommandContext> {
    private static final Mono<Result> COMMAND_NOT_FOUND = Mono.just(CommandNotFoundResult.get());
    private static final Object[] EMPTY_BEANS = new Object[0];

    private final CommandMap commandMap;
    private final ReactiveArgumentParser argumentParser;
    private final Tokeniser tokeniser;
    private final ImmutableList<ReactiveModule> modules;

    private ReactiveCommandHandler(
            CommandMap commandMap,
            ReactiveArgumentParser argumentParser,
            Tokeniser tokeniser,
            ImmutableList<ReactiveModule> modules) {
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

                var result = (TokeniserSuccessfulResult) result0;
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
        ReactiveCommand command = parserResult.command();
        context.command = command;
        var beans = getBeans(context, command.module.beans);
        return command.commandCallback
            .execute(context, beans, parserResult.parsedArguments())
            .cast(Result.class);
    }

    /**
     * @return All of the modules that belong to the CommandHandler.
     */
    public ImmutableList<ReactiveModule> modules() {
        return modules;
    }

    /**
     * @return All of the commands that belong to the CommandHandler.
     */
    public Stream<ReactiveCommand> commands() {
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
        private final Map<Class<?>, ReactiveTypeParser<?>> typeParserByClass;
        private final CommandMap.Builder commandMap;
        private final List<Class<? extends ReactiveModuleBase<T>>> commandModules;

        private BeanProvider beanProvider;
        private ReactiveArgumentParser argumentParser;
        private Tokeniser tokeniser;

        private Builder() {
            this.typeParserByClass = new HashMap<>(PrimitiveReactiveTypeParserFactory.create());
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
        public <S> Builder<T> withTypeParser(Class<S> clazz, ReactiveTypeParser<S> parser) {
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
        public <S extends ReactiveModuleBase<T>> Builder<T> withModule(Class<S> moduleClazz) {
            Preconditions.checkNotNull(moduleClazz, "moduleClazz cannot be null");
            this.commandModules.add(moduleClazz);
            return this;
        }

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
                    .forEach(clazz -> withModule(clazz.asSubclass(ReactiveModuleBase.class)));

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
        public Builder<T> withArgumentParser(ReactiveArgumentParser argumentParser) {
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
        public ReactiveCommandHandler<T> build() {
            List<ReactiveModule> modules = new ArrayList<>();
            CommandModuleFactory<T, ReactiveModuleBase<T>> moduleFactory = new CommandModuleFactory<>(
                beanProvider,
                typeParserByClass
            );

            for (Class<? extends ReactiveModuleBase<T>> moduleClazz : commandModules) {
                ReactiveModule module = moduleFactory.create(moduleClazz);
                modules.add(module);
                commandMap.map(module);
            }

            if (argumentParser == null) {
                argumentParser = new DefaultReactiveArgumentParser(ImmutableMap.copyOf(typeParserByClass));
            }

            return new ReactiveCommandHandler<>(commandMap.build(), argumentParser, tokeniser, ImmutableList.copyOf(modules));
        }
    }
}
