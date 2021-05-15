package kboyle.oktane.core;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.ClassPath;
import kboyle.oktane.core.exceptions.RuntimeIOException;
import kboyle.oktane.core.mapping.CommandMap;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.CommandModule;
import kboyle.oktane.core.module.ModuleBase;
import kboyle.oktane.core.module.Precondition;
import kboyle.oktane.core.module.factory.CommandModuleFactory;
import kboyle.oktane.core.module.factory.PreconditionFactory;
import kboyle.oktane.core.module.factory.PreconditionFactoryMap;
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

import static kboyle.oktane.core.CommandUtils.isValidModuleClass;

/**
 * The entry point for executing commands.
 *
 * {@code
 * CommandHandler.<YourContextType>builder()
 *     .withModule(YourCommandModule.class)
 *     .build();
 * }
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
     * Creates a new builder for the {@link CommandHandler}.
     *
     * @return A new {@link CommandHandler.Builder}.
     */
    public static <T extends CommandContext> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * Tries to execute a command for the given input.
     *
     * @param input The user input to parse.
     * @param context The {@link CommandContext} to pass into execution.
     * @return The result of execution.
     *
     * @throws NullPointerException when {@code input} is null.
     * @throws NullPointerException when {@code context} is null.
     */
    public Mono<Result> execute(String input, T context) {
        return execute(input, context, 0);
    }

    /**
     * Tries to execute a command for the given input.
     *
     * @param input The user input to parse.
     * @param context The {@link CommandContext} to pass into execution.
     * @param startIndex The index to start parsing the {@code input} from.
     * @return The result of execution.
     *
     * @throws NullPointerException when {@code input} is null.
     * @throws NullPointerException when {@code context} is null.
     * @throws IllegalStateException when {@code startIndex} &lt; 0.
     */
    public Mono<Result> execute(String input, T context, int startIndex) {
        Preconditions.checkNotNull(input);
        Preconditions.checkNotNull(context);
        Preconditions.checkState(startIndex > -1);

        if (input.isEmpty()) {
            return COMMAND_NOT_FOUND;
        }

        var matches = commandMap.findCommands(input, startIndex);

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
        var command = parserResult.command();
        context.command = command;
        var beans = getBeans(context, command.module.beans);
        return command.commandCallback
            .execute(context, beans, parserResult.parsedArguments())
            .cast(Result.class);
    }

    /**
     * @return All of the top level {@link CommandModule}s that belong to the {@link CommandHandler}.
     */
    public ImmutableList<CommandModule> modules() {
        return modules;
    }

    /**
     * @return Recursively gets all top level {@link CommandModule}s and their children that belong to the {@link CommandHandler}.
     */
    public Stream<CommandModule> flattenModules() {
        return modules.stream().flatMap(CommandUtils::flattenModule);
    }

    /**
     * @return All of the {@link Command}s that belong to the {@link CommandHandler}.
     */
    public Stream<Command> commands() {
        return modules.stream().flatMap(module -> module.commands.stream());
    }

    private Object[] getBeans(CommandContext context, ImmutableList<Class<?>> beanClasses) {
        if (beanClasses.isEmpty()) {
            return EMPTY_BEANS;
        }

        var beans = new Object[beanClasses.size()];
        for (var i = 0; i < beanClasses.size(); i++) {
            var beanClass = beanClasses.get(i);
            if (beanClass.equals(getClass())) {
                beans[i] = this;
                continue;
            }

            beans[i] = Preconditions.checkNotNull(
                context.beanProvider().getBean(beanClass),
                "A bean of type %s must be in your provider",
                beanClass
            );
        }

        return beans;
    }

    /**
     * A builder for the {@link CommandHandler}.
     *
     * @param <T> The type of context that's used in commands.
     */
    public static class Builder<T extends CommandContext> {
        private final Map<Class<?>, TypeParser<?>> typeParserByClass;
        private final CommandMap.Builder commandMap;
        private final List<Class<? extends ModuleBase<T>>> commandModules;
        private final PreconditionFactoryMap preconditionFactoryMap;

        private BeanProvider beanProvider;
        private ArgumentParser argumentParser;
        private Tokeniser tokeniser;

        private Builder() {
            this.typeParserByClass = new HashMap<>(PrimitiveTypeParserFactory.create());
            this.commandMap = CommandMap.builder();
            this.commandModules = new ArrayList<>();
            this.preconditionFactoryMap = new PreconditionFactoryMap();
            this.beanProvider = BeanProvider.empty();
            this.tokeniser = new DefaultTokeniser();
        }

        /**
         * Adds a {@link TypeParser} that will be used by the {@link CommandHandler}.
         *
         * @param cl The class representing the type that the {@link TypeParser} is for.
         * @param parser The {@link TypeParser}.
         * @param <S> The type that the {@link TypeParser} is for.
         * @return The {@link CommandHandler.Builder}.
         *
         * @throws NullPointerException when {@code cl} is null.
         * @throws NullPointerException when {@code parser} is null.
         */
        public <S> Builder<T> withTypeParser(Class<S> cl, TypeParser<S> parser) {
            Preconditions.checkNotNull(cl, "cl cannot be null");
            Preconditions.checkNotNull(parser, "Parser cannot be null");
            this.typeParserByClass.put(cl, parser);
            return this;
        }

        /**
         * Adds a {@link CommandModule} that will be used by the {@link CommandHandler}.
         *
         * @param moduleClass The class representing the type of module that you want to add.
         * @param <S> The type of module you want to add.
         * @return The {@link CommandHandler.Builder}.
         *
         * @throws NullPointerException when {@code moduleClass} is null.
         */
        public <S extends ModuleBase<T>> Builder<T> withModule(Class<S> moduleClass) {
            Preconditions.checkNotNull(moduleClass, "moduleClass cannot be null");
            this.commandModules.add(moduleClass);
            return this;
        }

        public Builder<T> withModules(Class<T> contextClass) {
            return withModules(contextClass, contextClass.getPackageName());
        }

        @SuppressWarnings("UnstableApiUsage")
        public Builder<T> withModules(Class<T> contextClass, String packageName) {
            try {
                ClassPath.from(contextClass.getClassLoader()).getTopLevelClassesRecursive(packageName)
                    .stream()
                    .map(ClassPath.ClassInfo::load)
                    .filter(cl -> isValidModuleClass(contextClass, cl))
                    .forEach(cl -> withModule(cl.asSubclass(ModuleBase.class)));

                return this;
            } catch (IOException exception) {
                throw new RuntimeIOException(exception);
            }
        }

        /**
         * Adds the {@link BeanProvider} that will be used to fetch dependencies for singleton {@link CommandModule}s.
         *
         * @param beanProvider The {@link BeanProvider}.
         * @return The {@link CommandHandler.Builder}.
         *
         * @throws NullPointerException when {@code beanProvider} is null.
         */
        public Builder<T> withBeanProvider(BeanProvider beanProvider) {
            Preconditions.checkNotNull(beanProvider, "beanProvider cannot be null");
            this.beanProvider = beanProvider;
            return this;
        }

        /**
         * Sets the {@link ArgumentParser} that will be used, if none is specified then the default will be used.
         *
         * @param argumentParser The {@link ArgumentParser}.
         * @return The {@link CommandHandler.Builder}.
         *
         * @throws NullPointerException when {@code argumentParser} is null.
         */
        public Builder<T> withArgumentParser(ArgumentParser argumentParser) {
            Preconditions.checkNotNull(argumentParser, "argumentParser cannot be null");
            this.argumentParser = argumentParser;
            return this;
        }

        /**
         * Sets the {@link Tokeniser} that will be used, if none is specified then the default will be used.
         *
         * @param tokeniser The {@link Tokeniser}.
         * @return The {@link CommandHandler.Builder}.
         *
         * @throws NullPointerException when {@code tokeniser} is null.
         */
        public Builder<T> withTokeniser(Tokeniser tokeniser) {
            Preconditions.checkNotNull(tokeniser, "tokeniser cannot be null");
            this.tokeniser = tokeniser;
            return this;
        }

        /**
         * Adds a {@link PreconditionFactory} that will be used to instantiate preconditions.
         *
         * @param factory The {@link PreconditionFactory} that will be used instead of {@link Precondition} of the given type.
         * @return The {@link CommandHandler.Builder}.
         *
         * @throws NullPointerException when {@code factory} is null.
         */
        public Builder<T> withPreconditionFactory(PreconditionFactory<?> factory) {
            preconditionFactoryMap.put(factory);
            return this;
        }

        /**
         * Builds the {@link CommandHandler}.
         *
         * @return The built {@link CommandHandler}.
         */
        public CommandHandler<T> build() {
            List<CommandModule> modules = new ArrayList<>();
            var moduleFactory = new CommandModuleFactory<T, ModuleBase<T>>(
                beanProvider,
                new HashMap<>(typeParserByClass),
                preconditionFactoryMap.copy()
            );

            for (var moduleClass : commandModules) {
                var module = moduleFactory.create(moduleClass);
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
