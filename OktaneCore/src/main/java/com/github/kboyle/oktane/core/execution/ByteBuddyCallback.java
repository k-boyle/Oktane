package com.github.kboyle.oktane.core.execution;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodCall;

import java.lang.reflect.Modifier;

public abstract class ByteBuddyCallback<C extends CommandContext, M extends ModuleBase<C>> implements CommandCallback {
    private final Class<M> moduleClass;

    public ByteBuddyCallback(Class<M> moduleClass) {
        this.moduleClass = moduleClass;
    }

    public static <C extends CommandContext, M extends ModuleBase<C>> ByteBuddyCallback<C, M> create(Class<C> contextClass, Class<M> moduleClass) throws NoSuchMethodException {
//        Class<?> contextClass = getContextClass(moduleClass);
//
//        new ByteBuddy()
//            .subclass(moduleClass)
//            .defineField("context", contextClass, Modifier.PRIVATE)
//            .defineConstructor(Modifier.PUBLIC)
//            .withParameters()


        new ByteBuddy()
            .subclass(AbstractCommandCallback.class)
            .defineMethod("getContext", contextClass, Modifier.PUBLIC)
            .withParameters(CommandContext.class)
            .intercept(MethodCall.invoke(ByteBuddyCallback.class.getDeclaredMethod("getContext")).onArgument(0));


        return null;
    }

    private static <C extends CommandContext> C getContext(Class<C> contextClass, CommandContext context) {
        if (contextClass.isInstance(context)) {
            return contextClass.cast(context);
        }

        throw new RuntimeException("Invalid context");
    }

//    private static Class<?> getContextClass(Type moduleClass) {
//        var superClass = ((Class<?>) moduleClass).getGenericSuperclass();
//        if (superClass == ModuleBase.class) {
//            return (Class<?>) ((ParameterizedType) superClass).getActualTypeArguments()[0];
//        }
//
//        return getContextClass(superClass);
//    }
}
