package kboyle.oktane.reactive.module;

import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.results.command.CommandExceptionResult;
import kboyle.oktane.reactive.results.command.CommandMessageResult;
import kboyle.oktane.reactive.results.command.CommandNOPResult;
import kboyle.oktane.reactive.results.command.CommandResult;
import reactor.core.publisher.Mono;

public abstract class CommandModuleBase<T extends CommandContext> {
    private T context;
    private Command command;

    protected T context() {
        return context;
    }

    protected Command command() {
        return command;
    }

    // todo figure out how to not expose this
    public void setContext(T context) {
        this.context = context;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    protected Mono<CommandResult> message(String reply) {
        return Mono.just(new CommandMessageResult(command, reply));
    }

    protected Mono<CommandResult> nop() {
        return Mono.just(new CommandNOPResult(command));
    }

    protected Mono<CommandResult> exception(Exception ex) {
        return Mono.just(new CommandExceptionResult(command, ex));
    }
}
