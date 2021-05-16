package kboyle.oktane.core.prefix;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.PrefixHandler;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents the default implementation of a {@link PrefixHandler}.
 *
 * @param <CONTEXT> The type of {@link CommandContext} to use.
 */
public class DefaultPrefixHandler<CONTEXT extends CommandContext> implements PrefixHandler<CONTEXT> {
    private final Set<Prefix<CONTEXT>> prefixes;

    public DefaultPrefixHandler() {
        prefixes = new HashSet<>();
    }

    @Override
    public Collection<Prefix<CONTEXT>> get(CommandContext context) {
        return prefixes;
    }

    /**
     * Adds a {@link Prefix} to the handle.
     *
     * @param prefix The {@link Prefix} to add.
     * @return The {@link DefaultPrefixHandler}.
     */
    public DefaultPrefixHandler<CONTEXT> addPrefix(Prefix<CONTEXT> prefix) {
        prefixes.add(prefix);
        return this;
    }
}
