package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.NaryOperatorNode;

public class NaryCompositeIQ extends AbstractCompositeIQ {
    protected NaryCompositeIQ(NaryOperatorNode rootNode, ImmutableList<IQ> subTrees) {
        super(rootNode, subTrees);
        if (subTrees.size() < 2)
            throw new IllegalArgumentException("At least two children are required for a n-ary node");
    }
}
