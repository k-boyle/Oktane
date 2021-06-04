package kboyle.oktane.core.module.callback;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.module.ModuleBase;
import kboyle.oktane.core.results.command.CommandResult;
import reactor.core.publisher.Mono;

public class SynchronisedCommandCallback<C extends CommandContext, M extends ModuleBase<C>> extends AnnotatedCommandCallback<C, M> {
    private final AnnotatedCommandCallback<C, M> delegate;

    public SynchronisedCommandCallback(AnnotatedCommandCallback<C, M> delegate) {
        this.delegate = delegate;
    }

    @Override
    public synchronized Mono<CommandResult> execute(M module, Object[] parameters) {
        return delegate.execute(module, parameters);
    }

    @Override
    public M getModule(Object[] beans) {
        return delegate.getModule(beans);
    }

    @Override
    public C getContext(CommandContext context) {
        return delegate.getContext(context);
    }
}
