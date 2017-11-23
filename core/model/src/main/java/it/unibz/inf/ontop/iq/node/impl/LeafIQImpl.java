package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.LeafIQ;
import it.unibz.inf.ontop.utils.VariableGenerator;

public abstract class LeafIQImpl extends QueryNodeImpl implements LeafIQ {

    private static final ImmutableList<IQ> EMPTY_LIST = ImmutableList.of();

    @Override
    public LeafIQ getRootNode() {
        return this;
    }

    @Override
    public ImmutableList<IQ> getChildren() {
        return EMPTY_LIST;
    }

    @Override
    public IQ liftBinding(VariableGenerator variableGenerator) {
        return this;
    }
}
