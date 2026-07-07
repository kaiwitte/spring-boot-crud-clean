package com.witteconsulting.cruddemo;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestUtil {
    /**
     * Creates a stream of all possible calls of combiner with the given Collection elements as arguments
     * (Cartesian product).
     * It's simplified like:
     * <pre>
     *     combiner.apply(first.get(0), second.get(0));
     *     combiner.apply(first.get(0), second.get(1));
     *     combiner.apply(first.get(1), second.get(0));
     *     ...
     * </pre>
     * @param first    collection of first method arguments
     * @param second   collection of second method arguments
     * @param combiner function that gets called with those arguments
     * @return a stream of results of calling combiner.apply on all combinations, so first.size()*second.size() is its length
     * @param <T> type of first method argument
     * @param <U> type of second method argument
     * @param <V> return type of method
     */
    static <T, U, V> Stream<V> allCombinations(
            final Collection<T> first, final Collection<U> second, final BiFunction<T, U, V> combiner) {
        return first.stream().flatMap(x -> second.stream().map(y -> combiner.apply(x, y)));
    }
}
