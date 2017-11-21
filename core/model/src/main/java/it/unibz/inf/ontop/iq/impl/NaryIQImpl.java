package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.NaryIQ;
import it.unibz.inf.ontop.iq.node.NaryOperatorNode;

public class NaryIQImpl extends AbstractCompositeIQ implements NaryIQ {

    @AssistedInject
    private NaryIQImpl(@Assisted NaryOperatorNode rootNode, @Assisted ImmutableList<IQ> subTrees) {
        super(rootNode, subTrees);
        if (subTrees.size() < 2)
            throw new IllegalArgumentException("At least two children are required for a n-ary node");
    }

    @Override
    public IQ liftBinding() {
        throw new RuntimeException("TODO: implement it");
    }
}
