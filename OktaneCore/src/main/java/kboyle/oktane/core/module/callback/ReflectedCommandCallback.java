package kboyle.oktane.core.module.callback;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.module.ModuleBase;
import kboyle.oktane.core.results.command.CommandResult;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;
import java.util.function.Function;

public class ReflectedCommandCallback<C extends CommandContext, M extends ModuleBase<C>> extends AnnotatedCommandCallback<C, M> {
    private final Function<Object[], M> moduleFactory;
    private final BiFunction<M, Object[], Mono<CommandResult>> callback;

    public ReflectedCommandCallback(
            Function<Object[], M> moduleFactory,
            BiFunction<M, Object[], Mono<CommandResult>> callback) {
        this.moduleFactory = moduleFactory;
        this.callback = callback;
    }

    @Override
    public M getModule(Object[] beans) {
        return moduleFactory.apply(beans);
    }

    @Override
    public Mono<CommandResult> execute(M module, Object[] parameters) {
        return callback.apply(module, parameters);
    }
}
