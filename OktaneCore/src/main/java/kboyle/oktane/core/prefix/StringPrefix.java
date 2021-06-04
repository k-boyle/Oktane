package kboyle.oktane.core.prefix;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import kboyle.oktane.core.CommandContext;

/**
 * Represents a {@link String} prefix.
 *
 * @param <CONTEXT> The type of {@link CommandContext}.
 */
public class StringPrefix implements Prefix {
    private final String prefix;

    public StringPrefix(String prefix) {
        this.prefix = Preconditions.checkNotNull(prefix, "prefix cannot be null");
    }

    @Override
    public int find(CommandContext context) {
        var input = context.input();
        return input.length() > prefix.length() && input.startsWith(prefix)
            ? prefix.length()
            : -1;
    }

    @Override
    public Object value() {
        return prefix;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("prefix", prefix)
            .toString();
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof StringPrefix other && other.prefix.equals(prefix);
    }

    @Override
    public int hashCode() {
        return prefix.hashCode();
    }
}
