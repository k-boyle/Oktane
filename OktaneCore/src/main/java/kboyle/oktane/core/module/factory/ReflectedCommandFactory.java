package kboyle.oktane.core.module.factory;

import kboyle.oktane.core.CollectionUtils;
import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.exceptions.FailedToInstantiateCommandCallback;
import kboyle.oktane.core.exceptions.MethodInvocationFailedException;
import kboyle.oktane.core.module.ModuleBase;
import kboyle.oktane.core.module.callback.ReflectedCommandCallback;
import kboyle.oktane.core.results.command.CommandResult;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.function.BiFunction;
import java.util.function.Function;

final class ReflectedCommandFactory {
    private ReflectedCommandFactory() {
    }

    static <C extends CommandContext, M extends ModuleBase<C>> ReflectedCommandCallback<C, M> createCallback(Class<M> moduleClass, Method method) {
        return new ReflectedCommandCallback<>(getModuleFactory(moduleClass), getCallbackFunction(method));
    }

    @SuppressWarnings("unchecked")
    private static <MODULE extends ModuleBase<?>> Function<Object[], MODULE> getModuleFactory(Class<MODULE> moduleClass) {
        var constructor = CollectionUtils.single(moduleClass.getConstructors());

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
    private static <MODULE extends ModuleBase<?>> BiFunction<MODULE, Object[], Mono<CommandResult>> getCallbackFunction(Method method) {
        return (module, parameters) -> {
            try {
                Object result;
                if (parameters.length == 0) {
                    result = method.invoke(module);
                } else {
                    result = method.invoke(module, parameters);
                }

                if (result instanceof CommandResult commandResult) {
                    return Mono.just(commandResult);
                }

                return (Mono<CommandResult>) result;
            } catch (Exception ex) {
                throw new MethodInvocationFailedException(ex);
            }
        };
    }
}
