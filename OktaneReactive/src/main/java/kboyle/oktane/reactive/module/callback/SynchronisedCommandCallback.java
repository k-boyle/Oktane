package kboyle.oktane.reactive.module.callback;

import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.module.ReactiveModuleBase;
import kboyle.oktane.reactive.results.command.CommandResult;
import reactor.core.publisher.Mono;

public class SynchronisedCommandCallback<C extends CommandContext, T extends ReactiveModuleBase<C>> extends AnnotatedCommandCallback<C, T> {
    private final AnnotatedCommandCallback<C, T> delegate;

    public SynchronisedCommandCallback(AnnotatedCommandCallback<C, T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public synchronized Mono<CommandResult> execute(T module, Object[] parameters) {
        return delegate.execute(module, parameters);
    }

    @Override
    public T getModule(Object[] beans) {
        return delegate.getModule(beans);
    }
}
