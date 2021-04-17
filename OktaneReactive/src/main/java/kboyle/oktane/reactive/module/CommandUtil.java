package kboyle.oktane.reactive.module;

import com.google.common.collect.ImmutableList;
import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.exceptions.FailedToInstantiatePreconditionException;
import kboyle.oktane.reactive.exceptions.InvalidConstructorException;
import kboyle.oktane.reactive.exceptions.UnhandledTypeException;
import kboyle.oktane.reactive.module.annotations.Require;
import kboyle.oktane.reactive.results.precondition.PreconditionResult;
import kboyle.oktane.reactive.results.precondition.PreconditionSuccessfulResult;
import kboyle.oktane.reactive.results.precondition.PreconditionsFailedResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.stream.Stream;

public final class CommandUtil {
    private static final Mono<PreconditionResult> SUCCESS = Mono.just(PreconditionSuccessfulResult.get());

    private CommandUtil() {
    }

    public static Mono<PreconditionResult> runPreconditions(CommandContext context, ReactiveCommand command, ImmutableList<ReactivePrecondition> preconditions) {
        if (preconditions.isEmpty()) {
            return SUCCESS;
        }

        return Flux.fromIterable(preconditions)
            .flatMap(precondition -> precondition.run(context, command))
            .collectList()
            .map(results -> {
                ImmutableList<PreconditionResult> failedResults = results.stream()
                    .filter(result -> !result.success())
                    .collect(ImmutableList.toImmutableList());

                if (failedResults.isEmpty()) {
                    return PreconditionSuccessfulResult.get();
                }

                return new PreconditionsFailedResult(failedResults);
            });
    }

    public static Stream<ReactivePrecondition> createPreconditions(AnnotatedElement element) {
        return Arrays.stream(element.getAnnotationsByType(Require.class))
            .map(CommandUtil::initPrecondition);
    }

    private static ReactivePrecondition initPrecondition(Require requirement) {
        Class<? extends ReactivePrecondition> clazz = requirement.precondition();
        String[] arguments = requirement.arguments();
        Constructor<?> validConstructor = Arrays.stream(clazz.getConstructors())
            .filter(CommandUtil::isValidConstructor)
            .reduce((single, other) -> {
                throw new InvalidConstructorException("Expected only a single constructor");
            })
            .orElseThrow(() -> new InvalidConstructorException("Expected at least one valid constructor"));
        try {
            if (arguments.length == 0) {
                return (ReactivePrecondition) validConstructor.newInstance();
            }

            return (ReactivePrecondition) validConstructor.newInstance((Object) arguments);
        } catch (Exception ex) {
            throw new FailedToInstantiatePreconditionException(clazz, ex);
        }
    }

    private static boolean isValidConstructor(Constructor<?> constructor) {
        Parameter[] parameters = constructor.getParameters();
        return parameters.length == 0 || parameters.length == 1 && parameters[0].getType().equals(String[].class);
    }

    public static <T extends CommandContext> boolean isValidModuleClass(Class<T> contextClazz, Class<?> moduleCandidate) {
        if (!ReactiveModuleBase.class.isAssignableFrom(moduleCandidate) || Modifier.isAbstract(moduleCandidate.getModifiers())) {
            return false;
        }

        ParameterizedType parameterizedType = unwrapModuleBase(moduleCandidate.getGenericSuperclass());
        if (parameterizedType.getRawType() != ReactiveModuleBase.class) {
            return isValidModuleClass(contextClazz, (Class<?>) parameterizedType.getRawType());
        }

        return parameterizedType.getActualTypeArguments()[0] == contextClazz;
    }

    public static ParameterizedType unwrapModuleBase(Type type) {
        if (type instanceof Class<?> clazz) {
            return unwrapModuleBase(clazz.getGenericSuperclass());
        } else if (type instanceof ParameterizedType parameterizedType) {
            if (parameterizedType.getRawType() == ReactiveModuleBase.class) {
                return parameterizedType;
            }
            return unwrapModuleBase(parameterizedType);
        }

        throw new UnhandledTypeException(type);
    }
}
