package kboyle.oktane.example.preconditions;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.Precondition;
import kboyle.oktane.core.results.precondition.PreconditionResult;
import reactor.core.publisher.Mono;

public class OrTest implements Precondition {
    private final String something;

    public OrTest(String[] args) {
        this.something = args[0];
    }

    @Override
    public Mono<PreconditionResult> run(CommandContext context, Command command) {
        return (something.equals("hi") ? success() : failure("something wrong")).mono();
    }
}
