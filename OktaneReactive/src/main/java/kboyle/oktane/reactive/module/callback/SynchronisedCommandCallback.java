package kboyle.oktane.reactive.module.callback;

import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.module.ReactiveModuleBase;
import kboyle.oktane.reactive.results.command.CommandResult;
import reactor.core.publisher.Mono;

public class SynchronisedCommandCallback<C extends CommandContext, M extends ReactiveModuleBase<C>> extends AnnotatedCommandCallback<C, M> {
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
}
