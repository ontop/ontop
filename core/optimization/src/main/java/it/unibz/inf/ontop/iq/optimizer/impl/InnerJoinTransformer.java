package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;

import java.util.Optional;

public interface InnerJoinTransformer {
    /**
     * Takes account of the interaction between children
     *
     * Returns empty() if no further optimization can be applied
     *
     */
    Optional<IQTree> transformInnerJoin(NaryIQTree tree, InnerJoinNode innerJoinNode, ImmutableList<IQTree> transformChildren);
}
