package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.QueryNode;

public class CompositeIQImpl implements IQ {

    private final QueryNode rootNode;
    private final ImmutableList<IQ> subTrees;

    protected CompositeIQImpl(QueryNode rootNode, ImmutableList<IQ> subTrees) {
        if (subTrees.isEmpty())
            throw new IllegalArgumentException("A composite IQ must have at least one child");
        this.rootNode = rootNode;
        this.subTrees = subTrees;
    }

    @Override
    public QueryNode getRootNode() {
        return rootNode;
    }

    @Override
    public ImmutableList<IQ> getSubTrees() {
        return subTrees;
    }
}
