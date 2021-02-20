package kboyle.oktane.core;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import kboyle.oktane.core.exceptions.InvalidResultException;
import kboyle.oktane.core.mapping.CommandMap;
import kboyle.oktane.core.mapping.CommandSearchResult;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.CommandModuleBase;
import kboyle.oktane.core.module.CommandModuleFactory;
import kboyle.oktane.core.module.Module;
import kboyle.oktane.core.parsers.ArgumentParser;
import kboyle.oktane.core.parsers.DefaultArgumentParser;
import kboyle.oktane.core.parsers.PrimitiveTypeParser;
import kboyle.oktane.core.parsers.TypeParser;
import kboyle.oktane.core.results.ExecutionErrorResult;
import kboyle.oktane.core.results.FailedResult;
import kboyle.oktane.core.results.Result;
import kboyle.oktane.core.results.argumentparser.SuccessfulArgumentParserResult;
import kboyle.oktane.core.results.precondition.PreconditionResult;
import kboyle.oktane.core.results.search.CommandMatchFailedResult;
import kboyle.oktane.core.results.search.CommandNotFoundResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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

        logger.trace("Finding command to execute from {}", input);

        ImmutableList<CommandSearchResult> searchResults = commandMap.findCommands(input);

        if (searchResults.isEmpty()) {
            return CommandNotFoundResult.get();
        }

        int pathLength = searchResults.get(0).pathLength();

        ImmutableList.Builder<FailedResult> failedResults = null;

        for (CommandSearchResult searchResult : searchResults) {
            if (searchResult.pathLength() < pathLength) {
                continue;
            }

            Command command = searchResult.command();
            context.command = command;

            logger.trace("Attempting to execute {}", command);

            try {
                PreconditionResult preconditionResult = command.runPreconditions(context);
                if (preconditionResult instanceof FailedResult failedResult) {
                    if (searchResults.size() == 1) {
                        return failedResult;
                    }

                    if (failedResults == null) {
                        failedResults = ImmutableList.builder();
                    }
                    failedResults.add(failedResult);
                    continue;
                }
            } catch (Exception ex) {
                return new ExecutionErrorResult(command, ex);
            }


            try {
                Result argumentParserResult = argumentParser.parse(context, searchResult.input(), searchResult.offset());
                Preconditions.checkNotNull(argumentParserResult, "Argument parser must return a non-null result");

                if (argumentParserResult instanceof FailedResult failedResult) {
                    if (searchResults.size() == 1) {
                        return failedResult;
                    }

                    if (failedResults == null) {
                        failedResults = ImmutableList.builder();
                    }
                    failedResults.add(failedResult);
                    continue;
                }

                logger.trace("Found command match, executing {}", command);

                if (argumentParserResult instanceof SuccessfulArgumentParserResult success) {
                    ImmutableList<Class<?>> beanClazzes = command.module().beans();
                    Object[] beans = getBeans(context, beanClazzes);
                    return command.commandCallback().execute(context, beans, success.parsedArguments());
                }

                throw new InvalidResultException(SuccessfulArgumentParserResult.class, argumentParserResult.getClass());
            } catch (InvalidResultException ir) {
                throw ir;
            } catch (Exception ex) {
                return new ExecutionErrorResult(command, ex);
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
    public Stream<Command> commands(){
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

        private BeanProvider beanProvider;
        private ArgumentParser argumentParser;

        private Builder() {
            this.typeParserByClass = new HashMap<>(PrimitiveTypeParser.DEFAULT_PARSERS);
            this.commandMap = CommandMap.builder();
            this.commandModules = new ArrayList<>();
            this.beanProvider = BeanProvider.get();
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

        /**
         * Builds the CommandHandler.
         * @return The built CommandHandler.
         */
        public CommandHandler<T> build() {
            List<Module> modules = new ArrayList<>();
            for (Class<? extends CommandModuleBase<T>> moduleClazz : commandModules) {
                Module module = CommandModuleFactory.create(moduleClazz, beanProvider);
                modules.add(module);
                commandMap.map(module);
            }

            if (argumentParser == null) {
                argumentParser = new DefaultArgumentParser(ImmutableMap.copyOf(typeParserByClass));
            }

            return new CommandHandler<>(commandMap.build(), argumentParser, ImmutableList.copyOf(modules));
        }
    }
}
