package kboyle.oktane.core.prefix;

import kboyle.oktane.core.CommandContext;
import kboyle.oktane.core.PrefixHandler;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents the default implementation of a {@link PrefixHandler}.
 */
public class DefaultPrefixHandler implements PrefixHandler {
    private final Set<Prefix> prefixes;

    public DefaultPrefixHandler() {
        prefixes = new HashSet<>();
    }

    @Override
    public Mono<Collection<Prefix>> get(CommandContext context) {
        return Mono.just(prefixes);
    }

    /**
     * Adds a {@link Prefix} to the handle.
     *
     * @param prefix The {@link Prefix} to add.
     * @return The {@link DefaultPrefixHandler}.
     */
    public DefaultPrefixHandler addPrefix(Prefix prefix) {
        prefixes.add(prefix);
        return this;
    }
}
