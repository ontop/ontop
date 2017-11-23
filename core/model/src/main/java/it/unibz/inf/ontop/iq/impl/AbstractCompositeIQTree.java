package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.CompositeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ExplicitVariableProjectionNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public abstract class AbstractCompositeIQTree<N extends QueryNode> implements CompositeIQTree<N> {

    private final N rootNode;
    private final ImmutableList<IQTree> children;

    /**
     * LAZY
     */
    @Nullable
    private ImmutableSet<Variable> knownVariables;

    protected AbstractCompositeIQTree(N rootNode, ImmutableList<IQTree> children) {
        if (children.isEmpty())
            throw new IllegalArgumentException("A composite IQ must have at least one child");
        this.rootNode = rootNode;
        this.children = children;
        // To be computed on-demand
        knownVariables = null;
    }

    @Override
    public N getRootNode() {
        return rootNode;
    }

    @Override
    public ImmutableList<IQTree> getChildren() {
        return children;
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        if (rootNode instanceof ExplicitVariableProjectionNode)
            return ((ExplicitVariableProjectionNode) rootNode).getVariables();
        else
            return children.stream()
                    .flatMap(c -> c.getVariables().stream())
                    .collect(ImmutableCollectors.toSet());
    }

    @Override
    public ImmutableSet<Variable> getKnownVariables() {
        if (knownVariables == null)
            knownVariables = Stream.concat(
                    getRootNode().getLocalVariables().stream(),
                    getChildren().stream()
                            .flatMap(c -> c.getKnownVariables().stream()))
                    .collect(ImmutableCollectors.toSet());
        return knownVariables;
    }
}
