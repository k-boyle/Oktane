package com.github.kboyle.oktane.core.execution;

import com.github.kboyle.oktane.core.result.command.CommandResult;

class ExternallySynchronisedCommandCallback<CONTEXT extends CommandContext, MODULE extends ModuleBase<CONTEXT>> extends AbstractCommandCallback<CONTEXT, MODULE> {
    private final AbstractCommandCallback<CONTEXT, MODULE> delegate;
    private final Object lock;

    ExternallySynchronisedCommandCallback(AbstractCommandCallback<CONTEXT, MODULE> delegate, Object lock) {
        this.delegate = delegate;
        this.lock = lock;
    }

    @Override
    protected CONTEXT getContext(CommandContext context) {
        return delegate.getContext(context);
    }

    @Override
    protected MODULE getModule(CONTEXT context) {
        return delegate.getModule(context);
    }

    @Override
    protected CommandResult execute(CONTEXT context, MODULE module) {
        synchronized (lock) {
            return delegate.execute(context, module);
        }
    }
}
