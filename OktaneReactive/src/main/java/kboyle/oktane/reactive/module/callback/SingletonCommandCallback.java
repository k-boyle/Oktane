package kboyle.oktane.reactive.module.callback;

import com.google.common.base.Preconditions;
import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.module.ReactiveModuleBase;
import kboyle.oktane.reactive.results.command.CommandResult;
import reactor.core.publisher.Mono;

public class SingletonCommandCallback<C extends CommandContext, T extends ReactiveModuleBase<C>> extends AnnotatedCommandCallback<C, T> {
    private final AnnotatedCommandCallback<C, T> delegate;
    private final T module;

    public SingletonCommandCallback(AnnotatedCommandCallback<C, T> delegate, T module) {
        this.delegate = delegate;
        this.module = Preconditions.checkNotNull(module);
    }

    @Override
    public Mono<CommandResult> execute(T module, Object[] parameters) {
        return delegate.execute(module, parameters);
    }

    @Override
    public T getModule(Object[] beans) {
        return module;
    }
}
