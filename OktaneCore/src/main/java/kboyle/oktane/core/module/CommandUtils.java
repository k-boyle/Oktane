package kboyle.oktane.core.module;

import com.google.common.collect.ImmutableList;
import kboyle.oktane.core.CollectionUtils;
import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.exceptions.FailedToInstantiateCommandCallback;
import kboyle.oktane.core.exceptions.FailedToInstantiatePreconditionException;
import kboyle.oktane.core.exceptions.MethodInvocationFailedException;
import kboyle.oktane.core.exceptions.UnhandledTypeException;
import kboyle.oktane.core.module.annotations.Require;
import kboyle.oktane.core.results.command.CommandResult;
import kboyle.oktane.core.results.precondition.PreconditionResult;
import kboyle.oktane.core.results.precondition.PreconditionSuccessfulResult;
import kboyle.oktane.core.results.precondition.PreconditionsFailedResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public final class CommandUtils {
    private static final Mono<PreconditionResult> SUCCESS = Mono.just(PreconditionSuccessfulResult.get());

    private CommandUtils() {
    }

    public static Mono<PreconditionResult> runPreconditions(CommandContext context, Command command, ImmutableList<Precondition> preconditions) {
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

    public static Stream<Precondition> createPreconditions(AnnotatedElement element) {
        return Arrays.stream(element.getAnnotationsByType(Require.class))
            .map(CommandUtils::initPrecondition);
    }

    private static Precondition initPrecondition(Require requirement) {
        Class<? extends Precondition> clazz = requirement.precondition();
        String[] arguments = requirement.arguments();
        Constructor<?> validConstructor = CollectionUtils.single(clazz.getConstructors());

        try {
            if (arguments.length == 0) {
                return (Precondition) validConstructor.newInstance();
            }

            return (Precondition) validConstructor.newInstance((Object) arguments);
        } catch (Exception ex) {
            throw new FailedToInstantiatePreconditionException(clazz, ex);
        }
    }

    private static boolean isValidConstructor(Constructor<?> constructor) {
        Parameter[] parameters = constructor.getParameters();
        return parameters.length == 0 || parameters.length == 1 && parameters[0].getType().equals(String[].class);
    }

    public static <T extends CommandContext> boolean isValidModuleClass(Class<T> contextClazz, Class<?> moduleCandidate) {
        if (!ModuleBase.class.isAssignableFrom(moduleCandidate) || Modifier.isAbstract(moduleCandidate.getModifiers())) {
            return false;
        }

        ParameterizedType parameterizedType = unwrapModuleBase(moduleCandidate.getGenericSuperclass());
        if (parameterizedType.getRawType() != ModuleBase.class) {
            return isValidModuleClass(contextClazz, (Class<?>) parameterizedType.getRawType());
        }

        return parameterizedType.getActualTypeArguments()[0] == contextClazz;
    }

    public static ParameterizedType unwrapModuleBase(Type type) {
        if (type instanceof Class<?> clazz) {
            return unwrapModuleBase(clazz.getGenericSuperclass());
        } else if (type instanceof ParameterizedType parameterizedType) {
            if (parameterizedType.getRawType() == ModuleBase.class) {
                return parameterizedType;
            }
            return unwrapModuleBase(parameterizedType);
        }

        throw new UnhandledTypeException(type);
    }

    @SuppressWarnings("unchecked")
    public static <MODULE extends ModuleBase<?>> Function<Object[], MODULE> getModuleFactory(Class<MODULE> moduleClass) {
        Constructor<?> constructor = CollectionUtils.single(moduleClass.getConstructors());

        return beans -> {
            try {
                if (beans.length == 0) {
                    return (MODULE) constructor.newInstance();
                }

                return (MODULE) constructor.newInstance(beans);
            } catch (Exception ex) {
                throw new FailedToInstantiateCommandCallback(ex);
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <MODULE extends ModuleBase<?>> BiFunction<MODULE, Object[], Mono<CommandResult>> getCallbackFunction(Method method) {
        return (module, parameters) -> {
            try {
                if (parameters.length == 0) {
                    return (Mono<CommandResult>) method.invoke(module);
                }

                return (Mono<CommandResult>) method.invoke(module, parameters);
            } catch (Exception ex) {
                throw new MethodInvocationFailedException(ex);
            }
        };
    }
}
