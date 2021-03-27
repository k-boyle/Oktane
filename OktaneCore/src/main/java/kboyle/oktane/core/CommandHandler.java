package kboyle.oktane.core;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.ClassPath;
import kboyle.oktane.core.exceptions.RuntimeIOException;
import kboyle.oktane.core.mapping.CommandMap;
import kboyle.oktane.core.mapping.CommandMatch;
import kboyle.oktane.core.module.Module;
import kboyle.oktane.core.module.*;
import kboyle.oktane.core.parsers.*;
import kboyle.oktane.core.results.Result;
import kboyle.oktane.core.results.argumentparser.ArgumentParserResult;
import kboyle.oktane.core.results.execution.ExecutionExceptionResult;
import kboyle.oktane.core.results.precondition.PreconditionResult;
import kboyle.oktane.core.results.search.CommandMatchFailedResult;
import kboyle.oktane.core.results.search.CommandNotFoundResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static kboyle.oktane.core.ReflectionUtil.isValidModuleClass;

/**
 * The entry point for executing commands.
 * @param <T> The type of context that's used in commands.
 */
public class CommandHandler<T extends CommandContext> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final Object[] EMPTY_BEANS = new Object[0];

    private final CommandMap commandMap;
    private final ArgumentParser argumentParser;
    private final ImmutableList<Module> modules;

    private CommandHandler(CommandMap commandMapper, ArgumentParser argumentParser, ImmutableList<Module> modules) {
        this.commandMap = commandMapper;
        this.argumentParser = argumentParser;
        this.modules = modules;
    }

    /**
     * Creates a new builder for the CommandHandler.
     * @return A new CommandHandler builder.
     */
    public static <T extends CommandContext> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * Attempts to execute a command based on the given input.
     * @param input The input to parse.
     * @param context The context for the command invocation.
     * @return A Mono holding the result of the execution.
     */
    public Result execute(String input, T context) {
        Preconditions.checkNotNull(input);
        Preconditions.checkNotNull(context);

        if (input.isEmpty()) {
            return CommandNotFoundResult.get();
        }

        logger.trace("Finding command to execute from {}", input);

        ImmutableList<CommandMatch> searchResults = commandMap.findCommands(input);

        if (searchResults.isEmpty()) {
            return CommandNotFoundResult.get();
        }

        int pathLength = searchResults.get(0).pathLength();

        ImmutableList.Builder<Result> failedResults = null;

        for (int i = 0, searchResultsSize = searchResults.size(); i < searchResultsSize; i++) {
            CommandMatch commandMatch = searchResults.get(i);
            if (commandMatch.pathLength() < pathLength) {
                continue;
            }

            Command command = commandMatch.command();
            context.command = command;

            logger.trace("Attempting to execute {}", command);

            try {
                PreconditionResult preconditionResult = command.runPreconditions(context);
                if (!preconditionResult.success()) {
                    if (searchResults.size() == 1) {
                        return preconditionResult;
                    }

                    if (failedResults == null) {
                        failedResults = ImmutableList.builder();
                    }
                    failedResults.add(preconditionResult);
                    continue;
                }
            } catch (Exception ex) {
                return new ExecutionExceptionResult(command, ex, ExecutionStep.PRECONDITIONS);
            }

            ArgumentParserResult argumentParserResult;
            try {
                argumentParserResult = argumentParser.parse(context, commandMatch, input);
                Preconditions.checkNotNull(argumentParserResult, "Argument parser must return a non-null result");

                if (!argumentParserResult.success()) {
                    if (searchResults.size() == 1) {
                        return argumentParserResult;
                    }

                    if (failedResults == null) {
                        failedResults = ImmutableList.builder();
                    }
                    failedResults.add(argumentParserResult);
                    continue;
                }
            } catch (Exception ex) {
                return new ExecutionExceptionResult(command, ex, ExecutionStep.ARGUMENT_PARSING);
            }

            Preconditions.checkNotNull(argumentParserResult.parsedArguments(), "Argument parser must return parsed arguments on success");

            ImmutableList<Class<?>> beanClazzes = command.module().beans();
            Object[] beans = getBeans(context, beanClazzes);

            try {
                logger.trace("Found command match, executing {}", command);
                return command.commandCallback().execute(context, beans, argumentParserResult.parsedArguments());
            } catch (Exception ex) {
                return new ExecutionExceptionResult(command, ex, ExecutionStep.COMMAND_EXECUTION);
            }
        }

        assert failedResults != null;
        return new CommandMatchFailedResult(failedResults.build());
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
     * @param <T> The type of context that's used in commands.
     */
    public static class Builder<T extends CommandContext> {
        private final Map<Class<?>, TypeParser<?>> typeParserByClass;
        private final CommandMap.Builder commandMap;
        private final List<Class<? extends CommandModuleBase<T>>> commandModules;
        private final Map<Class<? extends ArgumentParser>, ArgumentParser> argumentParserByClass;

        private BeanProvider beanProvider;
        private ArgumentParser argumentParser;

        private Builder() {
            this.typeParserByClass = new HashMap<>(PrimitiveTypeParserFactory.create());
            this.commandMap = CommandMap.builder();
            this.commandModules = new ArrayList<>();
            this.beanProvider = BeanProvider.empty();
            this.argumentParserByClass = new HashMap<>();
        }

        /**
         * Adds a type parser that will be used by the CommandHandler.
         * @param clazz The class representing the type that the TypeParser is for.
         * @param parser The TypeParser.
         * @param <S> The type that the TypeParser is for.
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
         * @param moduleClazz The class representing the type of module that you want to add.
         * @param <S> The type of module you want to add.
         * @return The builder.
         */
        public <S extends CommandModuleBase<T>> Builder<T> withModule(Class<S> moduleClazz) {
            Preconditions.checkNotNull(moduleClazz, "moduleClazz cannot be null");
            this.commandModules.add(moduleClazz);
            return this;
        }

        /**
         * Adds all the modules in the same package as the contextClazz.
         * @param contextClazz The class representing the type of context used for the CommandHandler.
         * @return The builder.
         */
        public Builder<T> withModules(Class<T> contextClazz) {
            return withModules(contextClazz, contextClazz.getPackageName());
        }

        /**
         * Adds all the modules in the specified package.
         * @param contextClazz The class representing the type of context used for the CommandHandler.
         * @param packageName The package name to search in.
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
         * @param argumentParser The argument parser.
         * @return The builder.
         */
        public Builder<T> withArgumentParser(ArgumentParser argumentParser) {
            Preconditions.checkNotNull(argumentParser, "argumentParser cannot be null");
            this.argumentParser = argumentParser;
            return this;
        }

        // todo doc
        public Builder<T> withArgumentParser0(ArgumentParser argumentParser) {
            Preconditions.checkNotNull(argumentParser);
            this.argumentParserByClass.put(argumentParser.getClass(), argumentParser);
            return this;
        }

        /**
         * Builds the CommandHandler.
         * @return The built CommandHandler.
         */
        public CommandHandler<T> build() {
            List<Module> modules = new ArrayList<>();
            CommandModuleFactory moduleFactory = new CommandModuleFactory(beanProvider, typeParserByClass, argumentParserByClass);
            for (Class<? extends CommandModuleBase<T>> moduleClazz : commandModules) {
                Module module = moduleFactory.create(moduleClazz);
                modules.add(module);
                commandMap.map(module);

                for (Command command : module.commands()) {
                    for (CommandParameter parameter : command.parameters()) {
                        if (parameter.type().isEnum()) {
                            // todo figure out how to handle this prior assigning parsers to parameters
                            typeParserByClass.computeIfAbsent(parameter.type(), clazz -> new EnumTypeParser(clazz));
                        }
                    }
                }
            }

            if (argumentParser == null) {
                argumentParser = new GenericArgumentParser(ImmutableMap.copyOf(typeParserByClass));
            }

            return new CommandHandler<>(commandMap.build(), argumentParser, ImmutableList.copyOf(modules));
        }
    }
}
