package com.github.kboyle.oktane.core.prefix;

import com.github.kboyle.oktane.core.execution.CommandContext;
import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.Collection;

public interface PrefixSupplier {
    Collection<Prefix<?>> get(CommandContext context);

    static PrefixSupplier empty() {
        return EmptyPrefixSupplier.get();
    }

    static PrefixSupplier of(Prefix<?> first, Prefix<?>... rest) {
        Preconditions.checkNotNull(first, "first cannot be null");
        Preconditions.checkNotNull(rest, "rest cannot be null");

        var prefixes = Arrays.asList(rest);
        prefixes.add(0, first);

        return new SimplePrefixSupplier(prefixes);
    }
}
