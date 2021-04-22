package kboyle.oktane.reactive.module.callback;

import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.module.ReactiveModuleBase;
import kboyle.oktane.reactive.results.command.CommandResult;
import reactor.core.publisher.Mono;

public class GloballySynchronisedCommandCallback<C extends CommandContext, M extends ReactiveModuleBase<C>> extends AnnotatedCommandCallback<C, M> {
    private final AnnotatedCommandCallback<C, M> delegate;
    private final Object lock;

    public GloballySynchronisedCommandCallback(AnnotatedCommandCallback<C, M> delegate, Object lock) {
        this.delegate = delegate;
        this.lock = lock;
    }

    @Override
    public Mono<CommandResult> execute(M module, Object[] parameters) {
        synchronized (lock) {
            return delegate.execute(module, parameters);
        }
    }

    @Override
    public M getModule(Object[] beans) {
        return delegate.getModule(beans);
    }
}