package com.github.kboyle.oktane.reactive.command;

import com.github.kboyle.oktane.core.execution.CommandCallback;
import com.github.kboyle.oktane.core.execution.CommandContext;
import com.github.kboyle.oktane.core.result.command.CommandResult;
import com.github.kboyle.oktane.reactive.ReactiveUtils;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface ReactiveCommandCallback extends CommandCallback {
    Mono<CommandResult> executeReactive(CommandContext context);

    @Override
    default CommandResult execute(CommandContext context) {
        ReactiveUtils.REACTIVE_WARNING_LOGGER.warn("Executing a reactive component [{}#execute] in a blocking manner", getClass());
        return executeReactive(context).block();
    }
}
