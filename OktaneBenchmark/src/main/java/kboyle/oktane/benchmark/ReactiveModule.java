package kboyle.oktane.benchmark;

import kboyle.oktane.reactive.module.CommandModuleBase;
import kboyle.oktane.reactive.module.annotations.Aliases;
import kboyle.oktane.reactive.results.command.CommandResult;
import reactor.core.publisher.Mono;

public class ReactiveModule extends CommandModuleBase<ReactiveContext> {
    @Aliases("echo")
    public Mono<CommandResult> echo(String input) {
        return message(input);
    }
}
