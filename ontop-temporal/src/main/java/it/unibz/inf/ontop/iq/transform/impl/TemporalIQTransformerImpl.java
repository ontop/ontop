package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.transform.TemporalIQTransformer;
import it.unibz.inf.ontop.temporal.iq.node.*;

public class TemporalIQTransformerImpl extends DefaultIdentityIQTransformer implements TemporalIQTransformer {
    @Override
    public IQTree transformBoxMinus(IQTree tree, BoxMinusNode rootNode, IQTree child) {
        return tree;
    }

    @Override
    public IQTree transformTemporalJoin(IQTree tree, TemporalJoinNode rootNode, ImmutableList<IQTree> children) {
        return tree;
    }

    @Override
    public IQTree transformBoxPlus(IQTree tree, BoxPlusNode rootNode, IQTree child) {
        return tree;
    }

    @Override
    public IQTree transformDiamondMinus(IQTree tree, DiamondMinusNode rootNode, IQTree child) {
        return tree;
    }

    @Override
    public IQTree transformDiamondPlus(IQTree tree, DiamondPlusNode rootNode, IQTree child) {
        return tree;
    }

    @Override
    public IQTree transformSince(IQTree tree, SinceNode rootNode, IQTree leftChild, IQTree rightChild) {
        return tree;
    }

    @Override
    public IQTree transformUntil(IQTree tree, UntilNode rootNode, IQTree leftChild, IQTree rightChild) {
        return tree;
    }

    @Override
    public IQTree transformTemporalCoalesce(IQTree tree, TemporalCoalesceNode rootNode, IQTree child) {
        return tree;
    }
}
