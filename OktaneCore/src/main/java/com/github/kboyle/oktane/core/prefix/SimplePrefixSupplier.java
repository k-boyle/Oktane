package com.github.kboyle.oktane.core.prefix;

import com.github.kboyle.oktane.core.execution.CommandContext;

import java.util.Collection;
import java.util.List;

class SimplePrefixSupplier implements PrefixSupplier {
    private final List<Prefix<?>> prefixes;

    SimplePrefixSupplier(Collection<Prefix<?>> prefixes) {
        this.prefixes = List.copyOf(prefixes);
    }

    @Override
    public Collection<Prefix<?>> get(CommandContext context) {
        return prefixes;
    }
}
