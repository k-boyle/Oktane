package kboyle.oktane.core.module.callback;

import com.google.common.base.Preconditions;
import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.module.ModuleBase;
import kboyle.oktane.core.results.command.CommandResult;
import reactor.core.publisher.Mono;

public class SingletonCommandCallback<C extends CommandContext, M extends ModuleBase<C>> extends AnnotatedCommandCallback<C, M> {
    private final AnnotatedCommandCallback<C, M> delegate;
    private final M module;

    public SingletonCommandCallback(AnnotatedCommandCallback<C, M> delegate, M module) {
        this.delegate = delegate;
        this.module = Preconditions.checkNotNull(module);
    }

    @Override
    public Mono<CommandResult> execute(M module, Object[] parameters) {
        return delegate.execute(module, parameters);
    }

    @Override
    public M getModule(Object[] beans) {
        return module;
    }

    @Override
    public C getContext(CommandContext context) {
        return delegate.getContext(context);
    }
}
