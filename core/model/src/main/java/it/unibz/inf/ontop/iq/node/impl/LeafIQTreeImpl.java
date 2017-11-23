package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.utils.VariableGenerator;

public abstract class LeafIQTreeImpl extends QueryNodeImpl implements LeafIQTree {

    private static final ImmutableList<IQTree> EMPTY_LIST = ImmutableList.of();

    @Override
    public LeafIQTree getRootNode() {
        return this;
    }

    @Override
    public ImmutableList<IQTree> getChildren() {
        return EMPTY_LIST;
    }

    @Override
    public IQTree liftBinding(VariableGenerator variableGenerator) {
        return this;
    }
}
