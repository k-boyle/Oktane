package com.github.kboyle.oktane.core.execution;

import com.github.kboyle.oktane.core.result.command.CommandResult;
import com.google.common.base.Preconditions;

class SingletonCommandCallback<CONTEXT extends CommandContext, MODULE extends ModuleBase<CONTEXT>> extends AbstractCommandCallback<CONTEXT, MODULE> {
    private final AbstractCommandCallback<CONTEXT, MODULE> delegate;
    private final Class<MODULE> moduleClass;

    public SingletonCommandCallback(AbstractCommandCallback<CONTEXT, MODULE> delegate, Class<MODULE> moduleClass) {
        this.delegate = delegate;
        this.moduleClass = moduleClass;
    }

    @Override
    protected CONTEXT getContext(CommandContext context) {
        return delegate.getContext(context);
    }

    @Override
    protected MODULE getModule(CONTEXT context) {
        var dependencyProvider = Preconditions.checkNotNull(context.applicationContext(), "dependencyProvider cannot be null");
        return Preconditions.checkNotNull(dependencyProvider.getBean(moduleClass), "Singleton modules must be provided by the Dependency Provider");
    }

    @Override
    protected CommandResult execute(CONTEXT context, MODULE module) {
        return delegate.execute(context, module);
    }
}
