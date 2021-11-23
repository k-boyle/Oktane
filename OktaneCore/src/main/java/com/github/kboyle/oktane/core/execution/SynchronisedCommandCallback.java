package com.github.kboyle.oktane.core.execution;

import com.github.kboyle.oktane.core.result.command.CommandResult;

class SynchronisedCommandCallback<CONTEXT extends CommandContext, MODULE extends ModuleBase<CONTEXT>> extends AbstractCommandCallback<CONTEXT, MODULE>{
    private final AbstractCommandCallback<CONTEXT, MODULE> delegate;

    SynchronisedCommandCallback(AbstractCommandCallback<CONTEXT, MODULE> delegate) {
        super(delegate.moduleClass);
        this.delegate = delegate;
    }

    @Override
    protected CONTEXT getContext(CommandContext context) {
        return delegate.getContext(context);
    }

    @Override
    protected synchronized CommandResult execute(CONTEXT context, MODULE module) {
        return delegate.execute(context, module);
    }
}
