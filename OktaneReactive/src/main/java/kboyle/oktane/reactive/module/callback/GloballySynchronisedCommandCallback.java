package kboyle.oktane.reactive.module.callback;

import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.module.ReactiveModuleBase;
import kboyle.oktane.reactive.results.command.CommandResult;
import reactor.core.publisher.Mono;

public class GloballySynchronisedCommandCallback<C extends CommandContext, T extends ReactiveModuleBase<C>> extends AnnotatedCommandCallback<C, T> {
    private final AnnotatedCommandCallback<C, T> delegate;
    private final Object lock;

    public GloballySynchronisedCommandCallback(AnnotatedCommandCallback<C, T> delegate, Object lock) {
        this.delegate = delegate;
        this.lock = lock;
    }

    @Override
    public Mono<CommandResult> execute(T module, Object[] parameters) {
        synchronized (lock) {
            return delegate.execute(module, parameters);
        }
    }

    @Override
    public T getModule(Object[] beans) {
        return delegate.getModule(beans);
    }
}
