package kboyle.oktane.reactivetest;

import kboyle.oktane.reactive.module.CommandModuleBase;
import kboyle.oktane.reactive.module.annotations.Aliases;
import kboyle.oktane.reactive.module.annotations.Remainder;
import kboyle.oktane.reactive.results.command.CommandResult;
import reactor.core.publisher.Mono;

public class Module extends CommandModuleBase<Context> {
    @Aliases("echo")
    public Mono<CommandResult> echo(@Remainder String input) {
        return message("echo: " + input);
    }
}
