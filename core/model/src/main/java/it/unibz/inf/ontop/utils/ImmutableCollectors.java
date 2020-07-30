package it.unibz.inf.ontop.utils;

import com.google.common.collect.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

/**
 * Inspired by http://codingjunkie.net/guava-and-java8-collectors/, as well as openJDK 8
 *
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

    private static class ImmutableMultisetCollector<T> extends ImmutableCollectionCollector<T, ImmutableMultiset.Builder<T>,
            ImmutableMultiset<T>> {
        @Override
        public Supplier<ImmutableMultiset.Builder<T>> supplier() {
            return ImmutableMultiset::builder;
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

    private static final class Partition<T> extends AbstractMap<Boolean, T> implements Map<Boolean, T> {
        final T forTrue;
        final T forFalse;

        Partition(T forTrue, T forFalse) {
            this.forTrue = forTrue;
            this.forFalse = forFalse;
        }

        @Override
        public Set<Map.Entry<Boolean, T>> entrySet() {
            return ImmutableSet.of(
                    new AbstractMap.SimpleImmutableEntry<>(false, forFalse),
                    new AbstractMap.SimpleImmutableEntry<>(true, forTrue)
            );
        }
    }


    public static <E> ImmutableListCollector<E> toList() {
        return new ImmutableListCollector<>();
    }

    public static <E> ImmutableSetCollector<E> toSet() {
        return new ImmutableSetCollector<>();
    }

    public static <E> ImmutableMultisetCollector<E> toMultiset() {
        return new ImmutableMultisetCollector<>();
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

    public static <T, K, U> Collector<T, ? ,ImmutableMap<K,U>> toMap(Function<? super T, ? extends K> keyMapper,
                                                                     Function<? super T, ? extends U> valueMapper,
                                                                     BinaryOperator<U> mergeFunction) {
        return Collector.of(
                // Supplier
                Maps::<K,U>newHashMap,
                // Accumulator
                (m, e) -> m.merge(keyMapper.apply(e), valueMapper.apply(e), mergeFunction),
                // Merger
                mapMerger(mergeFunction),
                // Finisher
                ImmutableMap::copyOf,
                Collector.Characteristics.UNORDERED);
    }

    private static <K, U>
    BinaryOperator<Map<K,U>> mapMerger(BinaryOperator<U> mergeFunction) {
        return (m1, m2) -> {
            for (Map.Entry<K,U> e : m2.entrySet())
                m1.merge(e.getKey(), e.getValue(), mergeFunction);
            return m1;
        };
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

    public static <T, K, U> Collector<T, ? ,ImmutableBiMap<K,U>> toBiMap(Function<? super T, ? extends K> keyMapper,
                                                                     Function<? super T, ? extends U> valueMapper) {
        return Collector.of(
                // Supplier
                ImmutableBiMap::<K,U>builder,
                // Accumulator
                (builder, e) -> builder.put(keyMapper.apply(e), valueMapper.apply(e)),
                // Merger
                (builder1, builder2) -> builder1.putAll(builder2.build()),
                // Finisher
                ImmutableBiMap.Builder::<K,U>build,
                Collector.Characteristics.UNORDERED);
    }

    public static <T, K, U> Collector<T, ? ,ImmutableBiMap<K,U>> toBiMap(Function<? super T, ? extends K> keyMapper,
                                                                     Function<? super T, ? extends U> valueMapper,
                                                                     BinaryOperator<U> mergeFunction) {
        return Collector.of(
                // Supplier
                Maps::<K,U>newHashMap,
                // Accumulator
                (m, e) -> m.merge(keyMapper.apply(e), valueMapper.apply(e), mergeFunction),
                // Merger
                mapMerger(mergeFunction),
                // Finisher
                ImmutableBiMap::copyOf,
                Collector.Characteristics.UNORDERED);
    }

    public static <T extends Map.Entry<K,U>, K, U> Collector<T, ? ,ImmutableBiMap<K,U>> toBiMap() {
        return Collector.of(
                // Supplier
                ImmutableBiMap::<K,U>builder,
                // Accumulator
                ImmutableBiMap.Builder::<K,U>put,
                // Merger
                (builder1, builder2) -> builder1.putAll(builder2.build()),
                // Finisher
                ImmutableBiMap.Builder::<K,U>build,
                Collector.Characteristics.UNORDERED);
    }

    public static <T extends Table.Cell<R,C,U>, R, C, U> Collector<T, ? ,ImmutableTable<R,C,U>> toTable() {
        return Collector.of(
                // Supplier
                ImmutableTable::<R,C,U>builder,
                // Accumulator
                ImmutableTable.Builder::<R,C,U>put,
                // Merger
                (builder1, builder2) -> builder1.putAll(builder2.build()),
                // Finisher
                ImmutableTable.Builder::<R,C,U>build,
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

    public static <T> Collector<T, ?, ImmutableMap<Boolean, ImmutableList<T>>> partitioningBy(Predicate<? super T> predicate) {
        return partitioningBy(predicate, toList());
    }


    public static <T, A , D> Collector<T, ?, ImmutableMap<Boolean, D>> partitioningBy (Predicate<? super T> predicate,
                                                                              Collector<T, A , D> innerCollector){

        //Supplier (stores a binary Partition, i.e. a (two entries) map from Boolean to the supplier type A of the
        // innerCollector)
        Supplier<Partition<A>> supplier = () -> new Partition<>(
                innerCollector.supplier().get(),
                innerCollector.supplier().get()
        );
        //Accumulator:
        BiConsumer<A, ? super T> downstreamAccumulator = innerCollector.accumulator();
        BiConsumer<Partition<A>, T> accumulator = (result, t) ->
                downstreamAccumulator.accept(predicate.test(t) ? result.forTrue : result.forFalse, t);

        //Merger
        BinaryOperator<A> op = innerCollector.combiner();
        BinaryOperator<Partition<A>> combiner = (left, right) ->
                new Partition<>(op.apply(left.forTrue, right.forTrue),
                        op.apply(left.forFalse, right.forFalse));
        //Finisher
        Function<Partition<A>, ImmutableMap<Boolean, D>> finisher = par -> ImmutableMap.of(
                true, innerCollector.finisher().apply(par.forTrue),
                false, innerCollector.finisher().apply(par.forFalse)
        );
        return Collector.of(supplier, accumulator, combiner, finisher, Collector.Characteristics.UNORDERED);
    }
}
