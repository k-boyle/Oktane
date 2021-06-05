package kboyle.oktane.core;

import com.google.common.collect.ImmutableList;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.CommandModule;
import kboyle.oktane.core.module.Precondition;
import kboyle.oktane.core.results.precondition.PreconditionResult;
import kboyle.oktane.core.results.precondition.PreconditionSuccessfulResult;
import kboyle.oktane.core.results.precondition.PreconditionsFailedResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Utilities for commands.
 */
public enum CommandUtils {
    ;

    private static final Mono<PreconditionResult> SUCCESS = Mono.just(PreconditionSuccessfulResult.get());

    /**
     * Runs the given preconditions.
     *
     * @param context The context to pass to {@link Precondition#run(CommandContext, Command)}.
     * @param command The command to pass to {@link Precondition#run(CommandContext, Command)}.
     * @param preconditions The {@link Precondition} to run.
     * @return The result of running all of the preconditions.
     */
    public static Mono<PreconditionResult> runPreconditions(CommandContext context, Command command, ImmutableList<Precondition> preconditions) {
        if (preconditions.isEmpty()) {
            return SUCCESS;
        }

        return Flux.fromIterable(preconditions)
            .flatMap(precondition -> precondition.run(context, command))
            .collectList()
            .map(results -> {
                var failedResults = results.stream()
                    .filter(result -> !result.success())
                    .collect(ImmutableList.toImmutableList());

                if (failedResults.isEmpty()) {
                    return PreconditionSuccessfulResult.get();
                }

                return new PreconditionsFailedResult(failedResults);
            });
    }

    /**
     * Gets a {@link CommandModule} and all its children.
     *
     * @param module The {@link CommandModule} to get it and all its children from.
     * @return The given {@link CommandModule} and all its children.
     */
    public static Stream<CommandModule> flattenModule(CommandModule module) {
        return Stream.of(module).mapMulti(CommandUtils::flattenModule);
    }

    private static void flattenModule(CommandModule module, Consumer<CommandModule> downStream) {
        downStream.accept(module);
        for (var child : module.children) {
            flattenModule(child, downStream);
        }
    }
}
