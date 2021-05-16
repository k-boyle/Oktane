package kboyle.oktane.core;

import com.google.common.base.Preconditions;
import kboyle.oktane.core.prefix.Prefix;

import java.util.Collection;
import java.util.function.Function;

/**
 * Represents a way to handle {@link Prefix}'s.
 *
 * @param <CONTEXT> The type of {@link CommandContext} to use.
 */
@FunctionalInterface
public interface PrefixHandler<CONTEXT extends CommandContext> extends Function<CONTEXT, Collection<Prefix<CONTEXT>>> {
    /**
     * Gets the {@link Prefix}'s for the given {@link CommandContext}.
     *
     * @param context The current execution {@link CommandContext}.
     * @return A collection of {@link Prefix}'s.
     */
    Collection<Prefix<CONTEXT>> get(CONTEXT context);

    /**
     * A proxy function for {@link #get(CommandContext)}.
     *
     * @param context The current execution {@link CommandContext}.
     * @return A collection of {@link Prefix}'s.
     */
    default Collection<Prefix<CONTEXT>> apply(CONTEXT context) {
        return get(context);
    }

    /**
     * Finds the index to start parsing from.
     *
     * @param context The current execution {@link CommandContext}.
     * @return The index to start parsing from, -1 if there is a missing prefix.
     */
    default int find(CONTEXT context) {
        var prefixes = Preconditions.checkNotNull(get(context), "Cannot return a null collection for prefixes");
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
    }
}
