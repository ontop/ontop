package it.unibz.inf.ontop.datalog.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.DistinctNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.UnaryOperatorNode;

/**
 * Lifts DISTINCT nodes above the highest construction node, as required by our Datalog data structure
 * <p>
 * TEMPORARY CODE (quickly implemented, does not cover all possible lift opportunities (e.g. injective functions))
 */
public class DistinctLifter {

    private final IntermediateQueryFactory iqFactory;

    @Inject
    private DistinctLifter(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    public IQTree liftDistinct(IQTree iqTree) {

        QueryNode root = iqTree.getRootNode();
        if (root instanceof ConstructionNode) {
            IQTree child = liftDistinct(((UnaryIQTree) iqTree).getChild());
            QueryNode childRoot = child.getRootNode();
            if (childRoot instanceof DistinctNode) {
                if (((ConstructionNode) root).getVariables().containsAll(child.getVariables())) {
                    IQTree distinctChild = ((UnaryIQTree) child).getChild();
                    return iqFactory.createUnaryIQTree(
                            (UnaryOperatorNode) childRoot,
                            iqFactory.createUnaryIQTree(
                                    (UnaryOperatorNode) root,
                                    distinctChild
                            ));
                }
            }
        }
        return iqTree;
    }
}

