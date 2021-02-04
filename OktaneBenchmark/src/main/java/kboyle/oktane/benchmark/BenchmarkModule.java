package kboyle.oktane.benchmark;

import kboyle.octane.core.module.CommandModuleBase;
import kboyle.octane.core.module.annotations.CommandDescription;
import kboyle.octane.core.module.annotations.ParameterDescription;
import kboyle.octane.core.results.command.CommandResult;
import reactor.core.publisher.Mono;

public class BenchmarkModule extends CommandModuleBase<BenchmarkCommandContext> {
    @CommandDescription(aliases = "a")
    public Mono<CommandResult> a() {
        return Mono.empty();
    }

    @CommandDescription(aliases = "b")
    public Mono<CommandResult> b(String arg1) {
        return Mono.empty();
    }

    @CommandDescription(aliases = "c")
    public Mono<CommandResult> c(@ParameterDescription(remainder = true) String arg1) {
        return Mono.empty();
    }

    @CommandDescription(aliases = "e")
    public Mono<CommandResult> e(int arg1) {
        return Mono.empty();
    }

    @CommandDescription(aliases = "f")
    public Mono<CommandResult> f(String one, String two, String three, String four, String five) {
        return Mono.empty();
    }
}
