package kboyle.oktane.reactive.module.callback;

import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.module.ReactiveModuleBase;
import kboyle.oktane.reactive.results.command.CommandResult;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;
import java.util.function.Function;

public class ReflectedCommandCallback<C extends CommandContext, T extends ReactiveModuleBase<C>> extends AnnotatedCommandCallback<C, T> {
    private final Function<Object[], T> moduleFactory;
    private final BiFunction<T, Object[], Mono<CommandResult>> callback;

    public ReflectedCommandCallback(
            Function<Object[], T> moduleFactory,
            BiFunction<T, Object[], Mono<CommandResult>> callback) {
        this.moduleFactory = moduleFactory;
        this.callback = callback;
    }

    @Override
    public T getModule(Object[] beans) {
        return moduleFactory.apply(beans);
    }

    @Override
    public Mono<CommandResult> execute(T module, Object[] parameters) {
        return callback.apply(module, parameters);
    }
}
