package kboyle.oktane.reactivetest;

import kboyle.oktane.reactive.module.ReactiveModuleBase;
import kboyle.oktane.reactive.module.annotations.Aliases;
import kboyle.oktane.reactive.module.annotations.Remainder;
import kboyle.oktane.reactive.results.command.CommandResult;
import reactor.core.publisher.Mono;

public class Module extends ReactiveModuleBase<Context> {
    @Aliases("echo")
    public Mono<CommandResult> echo(@Remainder String input) {
        return message("echo: " + input);
    }

    @Aliases("add")
    public Mono<CommandResult> add(int a, int b) {
        return message(a + b + "");
    }

    @Aliases("remainder")
    public Mono<CommandResult> remainder(String a, @Remainder String b) {
        return message("a: " + a + " b: " + b);
    }
}
