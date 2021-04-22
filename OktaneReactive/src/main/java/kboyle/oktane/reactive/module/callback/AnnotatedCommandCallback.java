package kboyle.oktane.reactive.module.callback;

import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.module.ReactiveModuleBase;
import kboyle.oktane.reactive.results.command.CommandExceptionResult;
import kboyle.oktane.reactive.results.command.CommandResult;
import reactor.core.publisher.Mono;

public abstract class AnnotatedCommandCallback<C extends CommandContext, M extends ReactiveModuleBase<C>> implements ReactiveCommandCallback {
    public abstract M getModule(Object[] beans);

    public abstract Mono<CommandResult> execute(M module, Object[] parameters);

    @SuppressWarnings("unchecked")
    @Override
    public Mono<CommandResult> execute(CommandContext context, Object[] beans, Object[] parameters) {
        try {
            M module = getModule(beans);
            C castedContext = (C) context;
            module.setContext(castedContext);
            return execute(module, parameters);
        } catch (Exception ex) {
            return Mono.just(new CommandExceptionResult(context.command(), ex));
        }
    }
}
