package kboyle.oktane.core.module.callback;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.module.ModuleBase;
import kboyle.oktane.core.module.ModuleBaseProxy;
import kboyle.oktane.core.results.command.CommandExceptionResult;
import kboyle.oktane.core.results.command.CommandResult;
import reactor.core.publisher.Mono;

public abstract class AnnotatedCommandCallback<C extends CommandContext, M extends ModuleBase<C>> implements CommandCallback {
    public abstract M getModule(Object[] beans);

    public abstract Mono<CommandResult> execute(M module, Object[] parameters);

    public abstract C getContext(CommandContext context);

    @Override
    public Mono<CommandResult> execute(CommandContext context, Object[] beans, Object[] parameters) {
        try {
            var module = getModule(beans);
            var castedContext = getContext(context);
            ModuleBaseProxy.setContext(module, castedContext);
            return execute(module, parameters);
        } catch (Exception ex) {
            return Mono.just(new CommandExceptionResult(context.command(), ex));
        }
    }
}
