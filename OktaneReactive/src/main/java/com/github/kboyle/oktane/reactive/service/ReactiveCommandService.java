package com.github.kboyle.oktane.reactive.service;

import com.github.kboyle.oktane.core.command.Command;
import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.execution.CommandService;
import com.github.kboyle.oktane.core.result.Result;
import reactor.core.publisher.Mono;

public interface ReactiveCommandService extends CommandService {
    Mono<Result> executeReactive(CommandContext context, Command command, Object[] parameters);
    Mono<Result> executeReactive(CommandContext context, String input, int startIndex);

    default Mono<Result> executeReactive(CommandContext context, String input) {
        return executeReactive(context, input, 0);
    }

    // todo not this
    @Override
    default Result execute(CommandContext context, Command command, Object[] arguments) {
        throw new UnsupportedOperationException();
    }

    @Override
    default Result execute(CommandContext context, String input, int startIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    default Result execute(CommandContext context, String input) {
        throw new UnsupportedOperationException();
    }
}
