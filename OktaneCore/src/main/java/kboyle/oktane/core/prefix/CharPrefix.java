package kboyle.oktane.core.prefix;

import com.google.common.base.MoreObjects;
import kboyle.oktane.core.CommandContext;

/**
 * Represents a {@link Prefix} that is only a single character.
 *
 * @param <CONTEXT>
 */
public class CharPrefix implements Prefix {
    private final String prefix;

    public CharPrefix(char prefix) {
        this.prefix = String.valueOf(prefix);
    }

    @Override
    public int find(CommandContext context) {
        var input = context.input();
        return input.startsWith(String.valueOf(prefix))
            ? 1
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
        return this == o || o instanceof CharPrefix other && other.prefix.equals(prefix);
    }

    @Override
    public int hashCode() {
        return prefix.hashCode();
    }
}
