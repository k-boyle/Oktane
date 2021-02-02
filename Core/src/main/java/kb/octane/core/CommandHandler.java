package kb.octane.core;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import kb.octane.core.exceptions.InvalidResultException;
import kb.octane.core.mapping.CommandMap;
import kb.octane.core.mapping.CommandSearchResult;
import kb.octane.core.module.Command;
import kb.octane.core.module.CommandModuleBase;
import kb.octane.core.module.CommandModuleFactory;
import kb.octane.core.module.Module;
import kb.octane.core.parsers.ArgumentParser;
import kb.octane.core.parsers.DefaultArgumentParser;
import kb.octane.core.parsers.PrimitiveTypeParser;
import kb.octane.core.parsers.TypeParser;
import kb.octane.core.results.ExecutionErrorResult;
import kb.octane.core.results.FailedResult;
import kb.octane.core.results.Result;
import kb.octane.core.results.argumentparser.SuccessfulArgumentParserResult;
import kb.octane.core.results.precondition.PreconditionResult;
import kb.octane.core.results.search.CommandMatchFailedResult;
import kb.octane.core.results.search.CommandNotFoundResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandHandler<T extends CommandContext> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final Object[] EMPTY_BEANS = new Object[0];

    private final CommandMap commandMap;
    private final ArgumentParser argumentParser;

    private CommandHandler(CommandMap commandMapper, ArgumentParser argumentParser) {
        this.commandMap = commandMapper;
        this.argumentParser = argumentParser;
    }

    public static <T extends CommandContext> Builder<T> builderForContext(Class<T> contextClazz) {
        return new Builder<>(contextClazz);
    }

    public Mono<Result> executeAsync(String input, T context) {
        Preconditions.checkNotNull(input);
        Preconditions.checkNotNull(context);

        logger.trace("Finding command to execute from {}", input);

        ImmutableList<CommandSearchResult> searchResults = commandMap.findCommands(input);

        if (searchResults.isEmpty()) {
            return Mono.just(CommandNotFoundResult.get());
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
                        return Mono.just(failedResult);
                    }

                    if (failedResults == null) {
                        failedResults = ImmutableList.builder();
                    }
                    failedResults.add(failedResult);
                    continue;
                }
            } catch (Exception ex) {
                return Mono.just(new ExecutionErrorResult(command, ex));
            }


            try {
                Result argumentParserResult = argumentParser.parse(context, searchResult.input(), searchResult.offset());
                Preconditions.checkNotNull(argumentParserResult, "Argument parser must return a non-null result");

                if (argumentParserResult instanceof FailedResult failedResult) {
                    if (searchResults.size() == 1) {
                        return Mono.just(failedResult);
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
                    return command.commandCallback().execute(context, beans, success.parsedArguments()).cast(Result.class);
                }

                throw new InvalidResultException(SuccessfulArgumentParserResult.class, argumentParserResult.getClass());
            } catch (InvalidResultException ir) {
                throw ir;
            } catch (Exception ex) {
                return Mono.just(new ExecutionErrorResult(command, ex));
            }
        }

        assert failedResults != null;
        return Mono.just(new CommandMatchFailedResult(failedResults.build()));
    }

    private static Object[] getBeans(CommandContext context, ImmutableList<Class<?>> beanClazzes) {
        if (beanClazzes.isEmpty()) {
            return EMPTY_BEANS;
        }

        Object[] beans = new Object[beanClazzes.size()];
        for (int i = 0; i < beanClazzes.size(); i++) {
            Class<?> beanClazz = beanClazzes.get(i);
            beans[i] = Preconditions.checkNotNull(
                context.beanProvider().getBean(beanClazz),
                "A bean of type %s must be in your provider",
                beanClazz
            );
        }

        return beans;
    }

    public static class Builder<T extends CommandContext> {
        private final Class<T> contextClazz;
        private final Map<Class<?>, TypeParser<?>> typeParserByClass;
        private final CommandMap.Builder commandMap;
        private final List<Class<? extends CommandModuleBase<T>>> commandModules;

        private BeanProvider beanProvider;
        private ArgumentParser argumentParser;

        private Builder(Class<T> contextClazz) {
            this.contextClazz = contextClazz;
            this.typeParserByClass = new HashMap<>(PrimitiveTypeParser.DEFAULT_PARSERS);
            this.commandMap = CommandMap.builder();
            this.commandModules = new ArrayList<>();
            this.beanProvider = BeanProvider.get();
        }

        public <S >Builder<T> withTypeParser(Class<S> clazz, TypeParser<S> parser) {
            Preconditions.checkNotNull(clazz, "Clazz cannot be null");
            Preconditions.checkNotNull(parser, "Parser cannot be null");
            this.typeParserByClass.put(clazz, parser);
            return this;
        }

        public <S extends CommandModuleBase<T>> Builder<T> withModule(Class<S> moduleClazz) {
            Preconditions.checkNotNull(moduleClazz, "moduleClazz cannot be null");
            this.commandModules.add(moduleClazz);
            return this;
        }

        public Builder<T> withBeanProvider(BeanProvider beanProvider) {
            Preconditions.checkNotNull(beanProvider, "beanProvider cannot be null");
            this.beanProvider = beanProvider;
            return this;
        }

        public Builder<T> withArgumentParser(ArgumentParser argumentParser) {
            Preconditions.checkNotNull(argumentParser, "argumentParser cannot be null");
            this.argumentParser = argumentParser;
            return this;
        }

        public CommandHandler<T> build() {
            for (Class<? extends CommandModuleBase<T>> moduleClazz : commandModules) {
                Module module = CommandModuleFactory.create(contextClazz, moduleClazz, beanProvider);
                this.commandMap.map(module);
            }

            if (argumentParser == null) {
                argumentParser = new DefaultArgumentParser(ImmutableMap.copyOf(typeParserByClass));
            }

            return new CommandHandler<>(commandMap.build(), argumentParser);
        }
    }
}
