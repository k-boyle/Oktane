package kboyle.oktane.reactive;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.ClassPath;
import kboyle.oktane.reactive.exceptions.RuntimeIOException;
import kboyle.oktane.reactive.mapping.CommandMap;
import kboyle.oktane.reactive.module.Command;
import kboyle.oktane.reactive.module.CommandModuleBase;
import kboyle.oktane.reactive.module.CommandModuleFactory;
import kboyle.oktane.reactive.module.Module;
import kboyle.oktane.reactive.parsers.ArgumentParser;
import kboyle.oktane.reactive.parsers.DefaultArgumentParser;
import kboyle.oktane.reactive.parsers.PrimitiveTypeParserFactory;
import kboyle.oktane.reactive.parsers.TypeParser;
import kboyle.oktane.reactive.results.Result;
import kboyle.oktane.reactive.results.argumentparser.ArgumentParserSuccessfulResult;
import kboyle.oktane.reactive.results.search.CommandMatchFailedResult;
import kboyle.oktane.reactive.results.search.CommandNotFoundResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static kboyle.oktane.reactive.ReflectionUtil.isValidModuleClass;

/**
 * The entry point for executing commands.
 *
 * @param <T> The type of context that's used in commands.
 */
public class ReactiveCommandHandler<T extends CommandContext> {
    private static final Mono<Result> COMMAND_NOT_FOUND = Mono.just(CommandNotFoundResult.get());
    private static final Object[] EMPTY_BEANS = new Object[0];

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final CommandMap commandMap;
    // todo this
    private final DefaultArgumentParser argumentParser = new DefaultArgumentParser(ImmutableMap.of());
    private final ImmutableList<Module> modules;

    private ReactiveCommandHandler(CommandMap commandMapper, ArgumentParser argumentParser, ImmutableList<Module> modules) {
        this.commandMap = commandMapper;
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

    public Mono<Result> push(String input, T context) {
        Preconditions.checkNotNull(input);
        Preconditions.checkNotNull(context);

        if (input.isEmpty()) {
            return COMMAND_NOT_FOUND;
        }

        return Mono.just(input)
            .map(commandMap::findCommands)
            .flatMap(matches -> {
                if (matches.isEmpty()) {
                    return COMMAND_NOT_FOUND;
                }

                return Flux.fromIterable(matches)
                    .flatMap(match -> {
                        var preconditionResult0 = match.command().runPreconditions(context);
                        return preconditionResult0.flatMap(preconditionResult -> {
                            if (!preconditionResult.success()) {
                                return Mono.just(preconditionResult);
                            }

                            // todo another nested call here to tokenise first to save wrapping tokeniser results in argument parser
                            return argumentParser.parse(context, match, input);
                        });
                    })
                    .collectList()
                    .flatMap(results -> {
                        var lastResult = results.get(results.size() - 1);

                        if (!lastResult.success()) {
                            return Mono.just(new CommandMatchFailedResult(results));
                        }

                        var success = (ArgumentParserSuccessfulResult) lastResult;
                        // todo beans
                        return success.command().commandCallback().execute(context, null, success.parsedArguments());
                    });
            });
    }

    /**
     * @return All of the modules that belong to the CommandHandler.
     */
    public ImmutableList<Module> modules() {
        return modules;
    }

    /**
     * @return All of the commands that belong to the CommandHandler.
     */
    public Stream<Command> commands() {
        return modules.stream().flatMap(module -> module.commands().stream());
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
        private final List<Class<? extends CommandModuleBase<T>>> commandModules;

        private BeanProvider beanProvider;
        private ArgumentParser argumentParser;

        private Builder() {
            this.typeParserByClass = new HashMap<>(PrimitiveTypeParserFactory.create());
            this.commandMap = CommandMap.builder();
            this.commandModules = new ArrayList<>();
            this.beanProvider = BeanProvider.empty();
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
        public <S extends CommandModuleBase<T>> Builder<T> withModule(Class<S> moduleClazz) {
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
                    .forEach(clazz -> withModule(clazz.asSubclass(CommandModuleBase.class)));

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

        /**
         * Builds the CommandHandler.
         *
         * @return The built CommandHandler.
         */
        public ReactiveCommandHandler<T> build() {
            List<Module> modules = new ArrayList<>();
            CommandModuleFactory moduleFactory = new CommandModuleFactory(beanProvider, typeParserByClass);
            for (Class<? extends CommandModuleBase<T>> moduleClazz : commandModules) {
                Module module = moduleFactory.create(moduleClazz);
                modules.add(module);
                commandMap.map(module);
            }

            if (argumentParser == null) {
//                argumentParser = new DefaultArgumentParser();
            }

            return new ReactiveCommandHandler<>(commandMap.build(), argumentParser, ImmutableList.copyOf(modules));
        }
    }
}
