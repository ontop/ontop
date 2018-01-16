package it.unibz.inf.ontop.iq.transform;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.temporal.iq.node.*;

public interface TemporalIQTransformer extends IQTransformer {

    IQTree transformBoxMinus(IQTree tree, BoxMinusNode rootNode, IQTree child);

    IQTree transformTemporalJoin(IQTree tree, TemporalJoinNode rootNode, ImmutableList<IQTree> children);

    IQTree transformBoxPlus(IQTree tree, BoxPlusNode rootNode, IQTree child);

    IQTree transformDiamondMinus(IQTree tree, DiamondMinusNode rootNode, IQTree child);

    IQTree transformDiamondPlus(IQTree tree, DiamondPlusNode rootNode, IQTree child);

    IQTree transformSince(IQTree tree, SinceNode rootNode, IQTree leftChild, IQTree rightChild);

    IQTree transformUntil(IQTree tree, UntilNode rootNode, IQTree leftChild, IQTree rightChild);

    IQTree transformTemporalCoalesce(IQTree tree, TemporalCoalesceNode rootNode, IQTree child);
}
