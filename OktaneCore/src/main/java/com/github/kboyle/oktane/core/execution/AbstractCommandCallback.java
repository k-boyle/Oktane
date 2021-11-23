package com.github.kboyle.oktane.core.execution;

import com.github.kboyle.oktane.core.result.command.CommandExceptionResult;
import com.github.kboyle.oktane.core.result.command.CommandResult;
import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public abstract class AbstractCommandCallback<CONTEXT extends CommandContext, MODULE extends ModuleBase<CONTEXT>> implements CommandCallback {
    protected final Class<MODULE> moduleClass;

    protected AbstractCommandCallback(Class<MODULE> moduleClass) {
        this.moduleClass = moduleClass;
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public static <CONTEXT extends CommandContext, MODULE extends ModuleBase<CONTEXT>> AbstractCommandCallback<CONTEXT, MODULE> create(Class<MODULE> moduleClass, Method method) {
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
            .subclass(AbstractCommandCallback.class, ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING)
            .method(ElementMatchers.named("getContext"))
            .intercept(MethodDelegation.to(GetContextInterceptor.INSTANCE))
            .defineMethod("execute0", CommandResult.class, Modifier.PRIVATE)
            .withParameters(moduleClass, Object[].class)
            .intercept(commandCall)
            .method(ElementMatchers.named("execute").and(ElementMatchers.isAbstract()))
            .intercept(executeMethodCall)
            .make()
            .load(AbstractCommandCallback.class.getClassLoader())
            .getLoaded();

        return (AbstractCommandCallback<CONTEXT, MODULE>) callbackClass.getDeclaredConstructors()[0].newInstance(moduleClass);
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
        var context = Preconditions.checkNotNull(getContext(baseContext), "getContext cannot return null");
        var module = Preconditions.checkNotNull(getModule(context), "getModule cannot return null");
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

    protected MODULE getModule(CommandContext context) {
        return context.applicationContext().getBean(moduleClass);
    }

    protected abstract CONTEXT getContext(CommandContext context);

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
}
