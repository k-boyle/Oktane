package com.github.kboyle.oktane.core.prefix;

import com.github.kboyle.oktane.core.execution.CommandContext;

import java.util.Collection;
import java.util.List;

class EmptyPrefixSupplier implements PrefixSupplier {
    private static final EmptyPrefixSupplier INSTANCE = new EmptyPrefixSupplier();

    private EmptyPrefixSupplier() {
    }

    @Override
    public Collection<Prefix<?>> get(CommandContext context) {
        return List.of();
    }

    static EmptyPrefixSupplier get() {
        return INSTANCE;
    }
}
