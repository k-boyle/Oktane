package com.github.kboyle.oktane.core.execution;

import com.github.kboyle.oktane.core.result.command.CommandExceptionResult;
import com.github.kboyle.oktane.core.result.command.CommandResult;
import com.google.common.base.Preconditions;
import lombok.SneakyThrows;

import java.lang.reflect.*;

class ReflectedCommandCallback<CONTEXT extends CommandContext, MODULE extends ModuleBase<CONTEXT>> extends AbstractCommandCallback<CONTEXT, MODULE> {
    private final Class<MODULE> moduleClass;
    private final Method method;

    ReflectedCommandCallback(Class<MODULE> moduleClass, Method method) {
        this.moduleClass = Preconditions.checkNotNull(moduleClass, "moduleClass cannot be null");
        this.method = Preconditions.checkNotNull(method, "method cannot be null");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected CONTEXT getContext(CommandContext context) {
        return (CONTEXT) context;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected MODULE getModule(CONTEXT context, Object[] dependencies) {
        var ctor = (Constructor<MODULE>) moduleClass.getDeclaredConstructors()[0];

        try {
            return dependencies.length == 0
                ? ctor.newInstance()
                : ctor.newInstance(dependencies);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    @SneakyThrows
    protected CommandResult execute(CONTEXT context, MODULE module, Object[] arguments) {
        try {
            var result = arguments.length == 0
                ? method.invoke(module)
                : method.invoke(module, arguments);

            return (CommandResult) result;
        } catch (InvocationTargetException ex) {
            if (ex.getTargetException() instanceof Exception target) {
                return new CommandExceptionResult(context.command, target);
            }

            throw ex.getTargetException();
        }
    }
}
