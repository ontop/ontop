package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.node.UnaryOperatorNode;

public class UnaryIQImpl extends AbstractCompositeIQ {

    protected UnaryIQImpl(UnaryOperatorNode rootNode, IQ child) {
        super(rootNode, ImmutableList.of(child));
    }
}
