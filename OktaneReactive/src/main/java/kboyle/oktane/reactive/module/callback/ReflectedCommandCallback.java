package kboyle.oktane.reactive.module.callback;

import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.module.ReactiveModuleBase;
import kboyle.oktane.reactive.results.command.CommandResult;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;
import java.util.function.Function;

public class ReflectedCommandCallback<C extends CommandContext, M extends ReactiveModuleBase<C>> extends AnnotatedCommandCallback<C, M> {
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
