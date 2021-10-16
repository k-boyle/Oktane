package com.github.kboyle.oktane.core.execution;

import com.github.kboyle.oktane.core.result.command.CommandExceptionResult;
import com.github.kboyle.oktane.core.result.command.CommandResult;
import lombok.SneakyThrows;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.*;
import java.util.Arrays;

import static com.github.kboyle.oktane.core.Utilities.Streams.single;

public abstract class AbstractCommandCallback<CONTEXT extends CommandContext, MODULE extends ModuleBase<CONTEXT>> implements CommandCallback {
    @SneakyThrows
    public static <CONTEXT extends CommandContext, MODULE extends ModuleBase<CONTEXT>> AbstractCommandCallback<CONTEXT, MODULE> create(Class<MODULE> moduleClass, Method method) {
        var constructor = getConstructor(moduleClass);

        var constructorCall = MethodCall.construct(constructor)
            .withArgumentArrayElements(0)
            .withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC);

        var dependenciesCall = MethodCall.invoke(ElementMatchers.named("dependencies"))
            .onArgument(0);

        var getModuleMethodCall = MethodCall.invoke(ElementMatchers.definedMethod(ElementMatchers.named("getModule0")))
            .withMethodCall(dependenciesCall);

        var commandCall = MethodCall.invoke(method)
            .onArgument(0)
            .withArgumentArrayElements(1)
            .withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC);

        var argumentsCall = MethodCall.invoke(ElementMatchers.named("arguments"))
            .onArgument(0);

        var executeMethodCall = MethodCall.invoke(ElementMatchers.definedMethod(ElementMatchers.named("execute0")))
            .withArgument(1)
            .withMethodCall(argumentsCall)
            .withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC);

        var callbackClass = new ByteBuddy()
            .subclass(AbstractCommandCallback.class)
            .method(ElementMatchers.named("getContext"))
            .intercept(MethodDelegation.to(GetContextInterceptor.INSTANCE))
            .defineMethod("getModule0", moduleClass, Modifier.PRIVATE)
            .withParameters(Object[].class)
            .intercept(constructorCall)
            .method(ElementMatchers.named("getModule"))
            .intercept(getModuleMethodCall)
            .defineMethod("execute0", CommandResult.class, Modifier.PRIVATE)
            .withParameters(moduleClass, Object[].class)
            .intercept(commandCall)
            .method(ElementMatchers.named("execute").and(ElementMatchers.isAbstract()))
            .intercept(executeMethodCall)
            .make()
            .load(AbstractCommandCallback.class.getClassLoader())
            .getLoaded();

        //noinspection unchecked
        return (AbstractCommandCallback<CONTEXT, MODULE>) callbackClass.getDeclaredConstructors()[0].newInstance();
    }

    private static Constructor<?> getConstructor(Class<?> cl) {
        return single(Arrays.stream(cl.getDeclaredConstructors()));
    }

    public enum GetContextInterceptor {
        INSTANCE;

        @RuntimeType
        public Object getContext(CommandContext context) {
            return context;
        }
    }

    @Override
    public CommandResult execute(CommandContext baseContext) {
        var context = getContext(baseContext);
        var module = getModule(context);
        module.context = context;

        try {
            before(context, module);
            var result = execute(context, module);
            after(context, module);

            return result;
        } catch (Exception ex) {
            return new CommandExceptionResult(context.command, ex);
        }
    }

    protected abstract CONTEXT getContext(CommandContext context);

    protected abstract MODULE getModule(CONTEXT context);

    protected abstract CommandResult execute(CONTEXT context, MODULE module);

    // todo unify the befores
    protected void before(CONTEXT context, MODULE module) {
        module.before();
    }

    protected void after(CONTEXT context, MODULE module) {
        module.after();
    }

    public AbstractCommandCallback<CONTEXT, MODULE> synchronised() {
        return new SynchronisedCommandCallback<>(this);
    }

    public AbstractCommandCallback<CONTEXT, MODULE> synchronised(Object lock) {
        return new ExternallySynchronisedCommandCallback<>(this, lock);
    }

    public AbstractCommandCallback<CONTEXT, MODULE> singleton(Class<MODULE> moduleClass) {
        return new SingletonCommandCallback<>(this, moduleClass);
    }
}
