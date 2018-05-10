package it.unibz.inf.ontop.temporal.model.tree;

import com.google.common.base.Function;
import com.google.common.collect.TreeTraverser;

import static com.google.common.base.Preconditions.checkNotNull;

public class CustomTreeTraverser<T> {

    public static <T> TreeTraverser<T> using(
            final Function<T, ? extends Iterable<T>> nodeToChildrenFunction) {
        checkNotNull(nodeToChildrenFunction);
        return new TreeTraverser<T>() {
            @Override
            public Iterable<T> children(T root) {
                return nodeToChildrenFunction.apply(root);
            }
        };
    }
}
