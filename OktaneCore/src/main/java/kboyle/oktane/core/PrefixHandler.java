package kboyle.oktane.core;

import com.google.common.base.Preconditions;
import kboyle.oktane.core.prefix.Prefix;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * Represents a way to handle {@link Prefix}'s.
 *
 * @param <CONTEXT> The type of {@link CommandContext} to use.
 */
@FunctionalInterface
public interface PrefixHandler {
    /**
     * Gets the {@link Prefix}'s for the given {@link CommandContext}.
     *
     * @param context The current execution {@link CommandContext}.
     * @return A collection of {@link Prefix}'s.
     */
    Mono<Collection<Prefix>> get(CommandContext context);

    /**
     * Finds the index to start parsing from.
     *
     * @param context The current execution {@link CommandContext}.
     * @return The index to start parsing from, -1 if there is a missing prefix.
     */
    default Mono<Integer> find(CommandContext context) {
        Preconditions.checkNotNull(context, "context cannot be null");
        return get(context)
            .map(prefixes -> {
                if (prefixes.isEmpty()) {
                    return 0;
                }

                for (var prefix : prefixes) {
                    int index = prefix.find(context);
                    if (index != -1) {
                        context.prefix = prefix;
                        return index;
                    }
                }

                return -1;
            });
    }
}
