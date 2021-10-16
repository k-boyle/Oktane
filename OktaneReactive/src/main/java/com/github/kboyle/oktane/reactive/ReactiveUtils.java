package com.github.kboyle.oktane.reactive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// todo not enum
public enum ReactiveUtils {
    ;

    public static final Logger REACTIVE_WARNING_LOGGER = LoggerFactory.getLogger("Blocking Warning");

    //    // todo these could be one
//    public static <R1, R2, T> Mono<R2> aggregateFailures(
//            Iterable<T> elements,
//            Function<T, Mono<R1>> monoSupplier,
//            Predicate<R1> failureCondition,
//            Function<List<R1>, Mono<R2>> aggregateFunction) {
//
//        Preconditions.checkNotNull(elements, "elements cannot be null");
//        Preconditions.checkNotNull(monoSupplier, "monoSupplier cannot be null");
//        Preconditions.checkNotNull(failureCondition, "failureCondition cannot be null");
//        Preconditions.checkNotNull(aggregateFunction, "aggregateFunction cannot be null");
//
//        return aggregateFailures(elements.iterator(), monoSupplier, failureCondition, aggregateFunction, new ArrayList<>());
//    }
//
//    private static <R1, R2, T> Mono<R2> aggregateFailures(
//            Iterator<T> iterator,
//            Function<T, Mono<R1>> monoSupplier,
//            Predicate<R1> failureCondition,
//            Function<List<R1>, Mono<R2>> aggregateFunction,
//            List<R1> aggregatorList) {
//
//        if (!iterator.hasNext()) {
//            return Preconditions.checkNotNull(
//                aggregateFunction.apply(aggregatorList),
//                "aggregateFunction result cannot be null"
//            );
//        }
//
//        var next = Preconditions.checkNotNull(iterator.next(), "iterator.next() cannot be null");
//        return Preconditions.checkNotNull(monoSupplier.apply(next), "monoSupplier result cannot be null")
//            .flatMap(result -> {
//                if (!failureCondition.test(result)) {
//                    aggregatorList.add(result);
//                }
//
//                return aggregateFailures(iterator, monoSupplier, failureCondition, aggregateFunction, aggregatorList);
//            });
//    }
//
//    public static <R1, R2, T> Mono<R2> executeUntil(
//            Iterable<T> elements,
//            Function<T, Mono<R1>> monoSupplier,
//            Predicate<R1> exitCondition,
//            Function<List<R1>, Mono<R2>> aggregateFunction,
//            Function<R1, Mono<R2>> exitMapper) {
//
//        Preconditions.checkNotNull(elements, "elements cannot be null");
//        Preconditions.checkNotNull(monoSupplier, "monoSupplier cannot be null");
//        Preconditions.checkNotNull(exitCondition, "exitCondition cannot be null");
//        Preconditions.checkNotNull(aggregateFunction, "aggregateFunction cannot be null");
//        Preconditions.checkNotNull(exitMapper, "exitMapper cannot be null");
//
//        return executeUntil(elements.iterator(), monoSupplier, exitCondition, aggregateFunction, exitMapper, new ArrayList<>());
//    }
//
//    private static <R1, R2, T> Mono<R2> executeUntil(
//            Iterator<T> iterator,
//            Function<T, Mono<R1>> monoSupplier,
//            Predicate<R1> exitCondition,
//            Function<List<R1>, Mono<R2>> aggregateFunction,
//            Function<R1, Mono<R2>> exitMapper,
//            List<R1> aggregatorList) {
//
//        if (!iterator.hasNext()) {
//            return Preconditions.checkNotNull(
//                aggregateFunction.apply(aggregatorList),
//                "aggregateFunction result cannot be null"
//            );
//        }
//
//        var next = Preconditions.checkNotNull(iterator.next(), "iterator.next() cannot be null");
//        return Preconditions.checkNotNull(monoSupplier.apply(next), "monoSupplier result cannot be null")
//            .flatMap(result -> {
//                if (exitCondition.test(result)) {
//                    return exitMapper.apply(result);
//                }
//
//                aggregatorList.add(result);
//                return executeUntil(iterator, monoSupplier, exitCondition, aggregateFunction, exitMapper, aggregatorList);
//            });
//    }
}
