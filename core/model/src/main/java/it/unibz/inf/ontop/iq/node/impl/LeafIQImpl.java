package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.LeafIQ;

public abstract class LeafIQImpl extends QueryNodeImpl implements LeafIQ {

    private static final ImmutableList<IQ> EMPTY_LIST = ImmutableList.of();

    @Override
    public LeafIQ getRootNode() {
        return this;
    }

    @Override
    public ImmutableList<IQ> getSubTrees() {
        return EMPTY_LIST;
    }
}
