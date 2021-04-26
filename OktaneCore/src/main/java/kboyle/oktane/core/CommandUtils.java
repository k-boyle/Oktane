package kboyle.oktane.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import kboyle.oktane.core.exceptions.FailedToInstantiatePreconditionException;
import kboyle.oktane.core.exceptions.UnhandledTypeException;
import kboyle.oktane.core.module.*;
import kboyle.oktane.core.module.annotations.Require;
import kboyle.oktane.core.module.annotations.RequireAny;
import kboyle.oktane.core.results.precondition.PreconditionResult;
import kboyle.oktane.core.results.precondition.PreconditionSuccessfulResult;
import kboyle.oktane.core.results.precondition.PreconditionsFailedResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.*;
import java.util.Arrays;
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

    /**
     * Creates all of the {@link Precondition}s from the {@link Require} annotations on the element.
     *
     * @param element The element to search for annotations on.
     * @return The created {@link Precondition}
     */
    public static Stream<Precondition> createPreconditions(AnnotatedElement element) {
        return Streams.concat(getANDPreconditions(element), getORPreconditions(element));
    }

    private static Stream<Precondition> getANDPreconditions(AnnotatedElement element) {
        return Arrays.stream(element.getAnnotationsByType(Require.class))
            .map(CommandUtils::initPrecondition);
    }

    private static Stream<Precondition> getORPreconditions(AnnotatedElement element) {
        return Arrays.stream(element.getAnnotationsByType(RequireAny.class))
            .map(CommandUtils::createAnyPrecondition);
    }

    private static AnyPrecondition createAnyPrecondition(RequireAny any) {
        return new AnyPrecondition(Arrays.stream(any.value())
            .map(CommandUtils::initPrecondition)
            .collect(ImmutableList.toImmutableList()));
    }

    private static Precondition initPrecondition(Require requirement) {
        var cl = requirement.precondition();
        var arguments = requirement.arguments();
        var validConstructor = CollectionUtils.single(cl.getConstructors(), CommandUtils::isValidConstructor);

        try {
            if (arguments.length == 0) {
                return (Precondition) validConstructor.newInstance();
            }

            return (Precondition) validConstructor.newInstance((Object) arguments);
        } catch (Exception ex) {
            throw new FailedToInstantiatePreconditionException(cl, ex);
        }
    }

    private static boolean isValidConstructor(Constructor<?> constructor) {
        var parameters = constructor.getParameters();
        return parameters.length == 0 || parameters.length == 1 && parameters[0].getType().equals(String[].class);
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
