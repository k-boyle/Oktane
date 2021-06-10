package kboyle.oktane.core;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.CommandModule;
import kboyle.oktane.core.module.CommandParameter;
import kboyle.oktane.core.module.Precondition;
import kboyle.oktane.core.results.precondition.ParameterPreconditionsFailedResult;
import kboyle.oktane.core.results.precondition.PreconditionResult;
import kboyle.oktane.core.results.precondition.PreconditionSuccessfulResult;
import kboyle.oktane.core.results.precondition.PreconditionsFailedResult;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Utilities for commands.
 */
public enum CommandUtils {
    ;

    private static final Mono<PreconditionResult> SUCCESS = Mono.just(PreconditionSuccessfulResult.get());

    /**
     * Runs the given {@link Precondition}s.
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

        return runPreconditions(context, command, preconditions.iterator(), new ArrayList<>());
    }

    /**
     * Runs the {@link Precondition}s for the parameters of the given {@link Command}.
     *
     * @param context The context to pass to {@link Precondition#run(CommandContext, Command)}.
     * @param command The command to pass to {@link Precondition#run(CommandContext, Command)}.
     * @return The result of running all of the preconditions.
     */
    public static Mono<PreconditionResult> runParameterPreconditions(CommandContext context, Command command) {
        Preconditions.checkState(
            command.parameters.size() == context.parsedArguments.length,
            "Mismatch between parameters and arguments"
        );

        if (command.parameters.isEmpty()) {
            return SUCCESS;
        }

        return runParameterPreconditions(context, command, 0, command.parameters, new ArrayList<>());
    }

    private static Mono<PreconditionResult> runParameterPreconditions(
            CommandContext context,
            Command command,
            int index,
            ImmutableList<CommandParameter> parameters,
            List<PreconditionResult> aggregatorList) {
        if (index == parameters.size()) {
            context.currentArgument = null;
            context.currentParameter = null;

            if (aggregatorList.isEmpty()) {
                return SUCCESS;
            }

            if (aggregatorList.size() == 1) {
                return aggregatorList.get(0).mono();
            }

            return new PreconditionsFailedResult(aggregatorList).mono();
        }

        var currentParameter = parameters.get(index);
        var preconditions = currentParameter.preconditions;
        if (preconditions.isEmpty()) {
            return runParameterPreconditions(context, command, index + 1, parameters, aggregatorList);
        }

        var currentArgument = context.parsedArguments[index];
        context.currentParameter = currentParameter;
        context.currentArgument = currentArgument;

        return runPreconditions(context, command, preconditions.iterator(), new ArrayList<>())
            .flatMap(result -> {
                if (!result.success()) {
                    aggregatorList.add(new ParameterPreconditionsFailedResult(currentParameter, currentArgument, result));
                }

                return runParameterPreconditions(context, command, index + 1, parameters, aggregatorList);
            });
    }

    private static Mono<PreconditionResult> runPreconditions(
            CommandContext context,
            Command command,
            Iterator<Precondition> preconditionIterator,
            List<PreconditionResult> aggregatorList) {
        if (!preconditionIterator.hasNext()) {
            if (aggregatorList.isEmpty()) {
                return SUCCESS;
            }

            if (aggregatorList.size() == 1) {
                return aggregatorList.get(0).mono();
            }

            return new PreconditionsFailedResult(aggregatorList).mono();
        }

        return preconditionIterator.next()
            .run(context, command)
            .flatMap(result -> {
                if (!result.success()) {
                    aggregatorList.add(result);
                }

                return runPreconditions(context, command, preconditionIterator, aggregatorList);
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
