package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.DistinctNode;
import it.unibz.inf.ontop.iq.node.SliceNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;

/**
 * TODO: find a better name
 */
public abstract class CaseInsensitiveIQTreeTransformerAdapter extends DefaultRecursiveIQTreeVisitingTransformer {

    protected CaseInsensitiveIQTreeTransformerAdapter(IntermediateQueryFactory iqFactory) {
        super(iqFactory);
    }

    protected abstract IQTree transformCardinalityInsensitiveTree(IQTree tree);

    @Override
    public IQTree transformDistinct(UnaryIQTree tree, DistinctNode rootNode, IQTree child) {
        return withTransformedChild(tree, transformCardinalityInsensitiveTree(child));
    }

    @Override
    public IQTree transformSlice(UnaryIQTree tree, SliceNode sliceNode, IQTree child) {
        // LIMIT 1
        if (sliceNode.getOffset() == 0 && sliceNode.getLimit().isPresent() && sliceNode.getLimit().getAsLong() <= 1) {
            return withTransformedChild(tree, transformCardinalityInsensitiveTree(child));
        }
        return super.transformSlice(tree, sliceNode, child);
    }
}
