package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.node.UnaryOperatorNode;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class UnaryIQTreeBuilder<T extends UnaryOperatorNode> {
    private final IntermediateQueryFactory iqFactory;
    private final ImmutableList<T> list;
    private final ImmutableMap<T, IQTreeCache> treeCacheMap;
    private final Optional<ImmutableSet<Variable>> signature;

    UnaryIQTreeBuilder(IntermediateQueryFactory iqFactory, ImmutableList<T> list, ImmutableMap<T, IQTreeCache> treeCacheMap, Optional<ImmutableSet<Variable>> signature) {
        this.iqFactory = iqFactory;
        this.list = list;
        this.treeCacheMap = treeCacheMap;
        this.signature = signature;
    }

    public UnaryIQTreeBuilder<T> append(T node) {
        return new UnaryIQTreeBuilder<>(
                iqFactory,
                Stream.concat(list.stream(), Stream.of(node))
                        .collect(ImmutableList.toImmutableList()),
                treeCacheMap,
                signature);
    }

    public UnaryIQTreeBuilder<T> append(T node, IQTreeCache treeCache) {
        return new UnaryIQTreeBuilder<>(
                iqFactory,
                Stream.concat(list.stream(), Stream.of(node))
                        .collect(ImmutableList.toImmutableList()),
                Stream.concat(treeCacheMap.entrySet().stream(), Stream.of(Maps.immutableEntry(node, treeCache)))
                        .collect(ImmutableCollectors.toMap()),
                signature);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public UnaryIQTreeBuilder<T> append(Optional<? extends T> optionalNode, Supplier<IQTreeCache> treeCacheSupplier) {
        if (optionalNode.isPresent())
            return append(optionalNode.get(), treeCacheSupplier.get());

        return this;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public UnaryIQTreeBuilder<T> append(Optional<? extends T> optionalNode) {
        if (optionalNode.isPresent())
            return append(optionalNode.get());

        return this;
    }

    public UnaryIQTreeBuilder<T> append(Stream<T> stream) {
        return new UnaryIQTreeBuilder<>(
                iqFactory,
                Stream.concat(list.stream(), stream).collect(ImmutableCollectors.toList()),
                treeCacheMap,
                signature);
    }

    public UnaryIQTreeBuilder<T> append(IQTreeTools.UnaryOperatorSequence<? extends T> sequence) {
        return sequence.stream()
                .reduce(this, UnaryIQTreeBuilder<T>::append,
                        ( c1, c2) -> { throw new MinorOntopInternalBugException("");});
    }

    public IQTree build(IQTree child) {
        IQTree current = child;
        for (T component : list.reverse()) {
            current = treeCacheMap.containsKey(component)
                    ? iqFactory.createUnaryIQTree(component, current, treeCacheMap.get(component))
                    : iqFactory.createUnaryIQTree(component, current);
        }
        if (signature.isPresent()) {
            ImmutableSet<Variable> variables = current.getVariables();
            if (variables.equals(signature.get()))
                return current;
            return iqFactory.createUnaryIQTree(
                    iqFactory.createConstructionNode(signature.get()), current);
        }
        return current;
    }
}
