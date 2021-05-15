package kboyle.oktane.core;

import com.google.common.collect.ImmutableList;
import kboyle.oktane.core.exceptions.UnhandledTypeException;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.CommandModule;
import kboyle.oktane.core.module.ModuleBase;
import kboyle.oktane.core.module.Precondition;
import kboyle.oktane.core.results.precondition.PreconditionResult;
import kboyle.oktane.core.results.precondition.PreconditionSuccessfulResult;
import kboyle.oktane.core.results.precondition.PreconditionsFailedResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Utilities for commands.
 */
public enum CommandUtils {
    ;

    private static final Mono<PreconditionResult> SUCCESS = Mono.just(PreconditionSuccessfulResult.get());

    /**
     * Runs the given preconditions.
     *
     * @param context The context to pass to {@link Precondition#run(CommandContext, Command)}.
     * @param command The command to pass to {@link Precondition#run(CommandContext, Command)}.
     * @param preconditions The {@link Precondition} to run.
     * @return The result of running all of the preconditions.
     */
    public static Mono<PreconditionResult> runPreconditions(CommandContext context, Command command, ImmutableList<Precondition> preconditions) {
        if (preconditions.isEmpty()) {
            return SUCCESS;
        }

        return Flux.fromIterable(preconditions)
            .flatMap(precondition -> precondition.run(context, command))
            .collectList()
            .map(results -> {
                var failedResults = results.stream()
                    .filter(result -> !result.success())
                    .collect(ImmutableList.toImmutableList());

                if (failedResults.isEmpty()) {
                    return PreconditionSuccessfulResult.get();
                }

                return new PreconditionsFailedResult(failedResults);
            });
    }

    static <T extends CommandContext> boolean isValidModuleClass(Class<T> contextClass, Class<?> moduleCandidate) {
        if (!ModuleBase.class.isAssignableFrom(moduleCandidate) || Modifier.isAbstract(moduleCandidate.getModifiers())) {
            return false;
        }

        var parameterizedType = unwrapModuleBase(moduleCandidate.getGenericSuperclass());
        if (parameterizedType.getRawType() != ModuleBase.class) {
            return isValidModuleClass(contextClass, (Class<?>) parameterizedType.getRawType());
        }

        return parameterizedType.getActualTypeArguments()[0] == contextClass;
    }

    private static ParameterizedType unwrapModuleBase(Type type) {
        if (type instanceof Class<?> cl) {
            return unwrapModuleBase(cl.getGenericSuperclass());
        } else if (type instanceof ParameterizedType parameterizedType) {
            if (parameterizedType.getRawType() == ModuleBase.class) {
                return parameterizedType;
            }
            return unwrapModuleBase(parameterizedType);
        }

        throw new UnhandledTypeException(type);
    }

    /**
     * Gets a {@link CommandModule} and all its children.
     *
     * @param module The {@link CommandModule} to get it and all its children from.
     * @return The given {@link CommandModule} and all its children.
     */
    public static Stream<CommandModule> flattenModule(CommandModule module) {
        return Stream.of(module).mapMulti(CommandUtils::flattenModule);
    }

    private static void flattenModule(CommandModule module, Consumer<CommandModule> downStream) {
        downStream.accept(module);
        for (var child : module.children) {
            flattenModule(child, downStream);
        }
    }
}
