package it.unibz.inf.ontop.datalog.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;

/**
 * Lifts SLICE nodes above the highest construction node, as required by our Datalog data structure
 * <p>
 * TEMPORARY CODE (quickly implemented)
 */
public class SliceLifter {

    private final IntermediateQueryFactory iqFactory;

    @Inject
    private SliceLifter(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    public IQTree liftSlice(IQTree iqTree) {

        QueryNode root = iqTree.getRootNode();

        if(root instanceof DistinctNode){
            return liftSlice(((UnaryIQTree) iqTree).getChild());
        }
        if (root instanceof ConstructionNode) {
            IQTree child = liftSlice(((UnaryIQTree) iqTree).getChild());
            QueryNode childRoot = child.getRootNode();
            if (childRoot instanceof SliceNode) {
                IQTree sliceChild = ((UnaryIQTree) child).getChild();
                return iqFactory.createUnaryIQTree(
                        (UnaryOperatorNode) childRoot,
                        iqFactory.createUnaryIQTree(
                                (UnaryOperatorNode) root,
                                sliceChild
                        ));
            }
        }
        return iqTree;
    }
}
