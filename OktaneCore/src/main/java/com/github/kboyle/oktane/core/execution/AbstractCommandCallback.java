package com.github.kboyle.oktane.core.execution;

import com.github.kboyle.oktane.core.result.command.CommandResult;

import java.lang.reflect.Method;

public abstract class AbstractCommandCallback<CONTEXT extends CommandContext, MODULE extends ModuleBase<CONTEXT>> implements CommandCallback {
    public static <CONTEXT extends CommandContext, MODULE extends ModuleBase<CONTEXT>> AbstractCommandCallback<CONTEXT, MODULE> reflection(Class<MODULE> moduleClass, Method method) {
        return new ReflectedCommandCallback<>(moduleClass, method);
    }

    @Override
    public CommandResult execute(CommandContext baseContext) {
        var context = getContext(baseContext);
        var module = getModule(context, context.dependencies);
        module.context = context;

        before(context, module);
        var result = execute(context, module, context.arguments);
        after(context, module);

        return result;
    }

    protected abstract CONTEXT getContext(CommandContext context);

    protected abstract MODULE getModule(CONTEXT context, Object[] dependencies);

    protected abstract CommandResult execute(CONTEXT context, MODULE module, Object[] arguments);

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
