package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.CompositeIQ;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.ExplicitVariableProjectionNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

public abstract class AbstractCompositeIQ implements CompositeIQ {

    private final QueryNode rootNode;
    private final ImmutableList<IQ> subTrees;

    protected AbstractCompositeIQ(QueryNode rootNode, ImmutableList<IQ> subTrees) {
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
    public ImmutableList<IQ> getChildren() {
        return subTrees;
    }

    @Override
    public ImmutableSet<Variable> getVariables() {
        if (rootNode instanceof ExplicitVariableProjectionNode)
            return ((ExplicitVariableProjectionNode) rootNode).getVariables();
        else
            return subTrees.stream()
                    .flatMap(c -> c.getVariables().stream())
                    .collect(ImmutableCollectors.toSet());
    }
}
