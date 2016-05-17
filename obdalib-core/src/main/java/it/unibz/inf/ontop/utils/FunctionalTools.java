package it.unibz.inf.ontop.utils;

import com.google.common.collect.ImmutableList;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * To alleviate what is missing in Java 8
 */
public class FunctionalTools {

    public static <S, T> ImmutableList<Map.Entry<S, T>> zip(List<S> list1, List<T> list2) {
        int length = Math.min(list1.size(), list2.size());

        ImmutableList.Builder<Map.Entry<S, T>> listBuilder = ImmutableList.builder();

        IntStream.range(0, length)
                .forEach(i -> listBuilder.add(new AbstractMap.SimpleEntry<>(list1.get(i), list2.get(i))));

        return listBuilder.build();
    }

}
