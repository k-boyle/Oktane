package kboyle.oktane.core;

import kboyle.oktane.core.exceptions.InvalidConstructorException;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class CollectionUtils {
    private CollectionUtils() {
    }

    public static <T> T single(Collection<T> collection) {
        return single(collection, t -> true);
    }

    public static <T> T single(Collection<T> collection, Predicate<T> filter) {
        return single(collection.stream(), filter);
    }

    public static <T> T single(T[] arr) {
        return single(arr, t -> true);
    }

    public static <T> T single(T[] arr, Predicate<T> filter) {
        return single(Arrays.stream(arr), filter);
    }

    private static <T> T single(Stream<T> stream, Predicate<T> filter) {
        return stream
            .filter(filter)
            .reduce((single, other) -> {
                throw new InvalidConstructorException("Expected only a single element");
            })
            .orElseThrow(() -> new InvalidConstructorException("Expected at least one element"));
    }
}
