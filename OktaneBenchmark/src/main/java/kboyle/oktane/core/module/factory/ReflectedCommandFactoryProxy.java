package kboyle.oktane.core.module.factory;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.module.ModuleBase;
import kboyle.oktane.core.module.callback.ReflectedCommandCallback;

import java.lang.reflect.Method;

public final class ReflectedCommandFactoryProxy {
    private ReflectedCommandFactoryProxy() {
    }

    public static <C extends CommandContext, M extends ModuleBase<C>> ReflectedCommandCallback<C, M> createCallback(Class<M> moduleClass, Method method) {
        return ReflectedCommandFactory.createCallback(moduleClass, method);
    }
}
