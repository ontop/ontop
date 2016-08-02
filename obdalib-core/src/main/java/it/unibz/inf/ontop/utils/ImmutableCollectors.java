package it.unibz.inf.ontop.utils;

import com.google.common.collect.*;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Inspired by http://codingjunkie.net/guava-and-java8-collectors/
 */
public class ImmutableCollectors {

    private static abstract class ImmutableCollectionCollector<T, A extends ImmutableCollection.Builder, R extends ImmutableCollection<T>>
            implements Collector<T, A, R> {

        @Override
        public BiConsumer<A, T> accumulator() {
            return (c, v) -> c.add(v);
        }

        @Override
        public BinaryOperator<A> combiner() {
            return (c1, c2) -> (A) c1.addAll(c2.build().iterator());
        }

        @Override
        public Function<A, R> finisher() {
            return (bl -> (R) bl.build());
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Sets.newHashSet(Collector.Characteristics.CONCURRENT);
        }
    }

    private static class ImmutableSetCollector<T> extends ImmutableCollectionCollector<T, ImmutableSet.Builder<T>,
            ImmutableSet<T>> {
        @Override
        public Supplier<ImmutableSet.Builder<T>> supplier() {
            return ImmutableSet::builder;
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Sets.newHashSet(Characteristics.CONCURRENT, Characteristics.UNORDERED);
        }
    }

    private static class ImmutableListCollector<T> extends ImmutableCollectionCollector<T, ImmutableList.Builder<T>,
            ImmutableList<T>> {
        @Override
        public Supplier<ImmutableList.Builder<T>> supplier() {
            return ImmutableList::builder;
        }
    }


    public static <E> ImmutableListCollector<E> toList() {
        return new ImmutableListCollector<>();
    }

    public static <E> ImmutableSetCollector<E> toSet() {
        return new ImmutableSetCollector<>();
    }

    public static <T, K, U> Collector<T, ? ,ImmutableMap<K,U>> toMap(Function<? super T, ? extends K> keyMapper,
                                                     Function<? super T, ? extends U> valueMapper) {
        return Collector.of(
                // Supplier
                ImmutableMap::<K,U>builder,
                // Accumulator
                (builder, e) -> builder.put(keyMapper.apply(e), valueMapper.apply(e)),
                // Merger
                (builder1, builder2) -> builder1.putAll(builder2.build()),
                // Finisher
                ImmutableMap.Builder::<K,U>build,
                Collector.Characteristics.UNORDERED);
    }

    public static <T extends Map.Entry<K,U>, K, U> Collector<T, ? ,ImmutableMap<K,U>> toMap() {
        return Collector.of(
                // Supplier
                ImmutableMap::<K,U>builder,
                // Accumulator
                ImmutableMap.Builder::<K,U>put,
                // Merger
                (builder1, builder2) -> builder1.putAll(builder2.build()),
                // Finisher
                ImmutableMap.Builder::<K,U>build,
                Collector.Characteristics.UNORDERED);
    }

    public static <T, K, U> Collector<T, ? ,ImmutableMultimap<K,U>> toMultimap(Function<? super T, ? extends K> keyMapper,
                                                                     Function<? super T, ? extends U> valueMapper) {
        return Collector.of(
                // Supplier
                ImmutableMultimap::<K,U>builder,
                // Accumulator
                (builder, e) -> builder.put(keyMapper.apply(e), valueMapper.apply(e)),
                // Merger
                (builder1, builder2) -> builder1.putAll(builder2.build()),
                // Finisher
                ImmutableMultimap.Builder::<K,U>build,
                Collector.Characteristics.UNORDERED);
    }

    public static <T extends Map.Entry<K,U>, K, U> Collector<T, ? ,ImmutableMultimap<K,U>> toMultimap() {
        return Collector.of(
                // Supplier
                ImmutableMultimap::<K,U>builder,
                // Accumulator
                ImmutableMultimap.Builder::<K,U>put,
                // Merger
                (builder1, builder2) -> builder1.putAll(builder2.build()),
                // Finisher
                ImmutableMultimap.Builder::<K,U>build,
                Collector.Characteristics.UNORDERED);
    }

}
