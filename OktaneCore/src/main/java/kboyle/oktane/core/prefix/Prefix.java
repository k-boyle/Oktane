package kboyle.oktane.core.prefix;

import kboyle.oktane.core.CommandContext;

/**
 * Represents a prefix for an input to passed to {@link kboyle.oktane.core.CommandHandler#execute(String, CommandContext)}.
 *
 * @param <CONTEXT> The type of {@link CommandContext} to use.
 */
public interface Prefix {
    /**
     * Returns the index after the prefix, -1 if not found.
     *
     * @param context The current execution {@link CommandContext}.
     * @return The index where the current prefix ends, -1 if not found.
     */
    int find(CommandContext context);

    Object value();
}
