package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;

/**
 *
 * Applies (i) a "set" of (pre)-transformers to the current tree, before
 *         (ii) using a child transformer to apply itself on the children, and
 *         (iii) applies another "set" of (post)-transformers to the current tree.
 *
 */
public final class CompositeIQTreeTransformer implements IQTreeTransformer {

    private final ImmutableList<IQTreeTransformer> preTransformers;
    private final ImmutableList<IQTreeTransformer> postTransformers;
    private final IQTreeTransformer childTransformer;

    public CompositeIQTreeTransformer(ImmutableList<IQTreeTransformer> preTransformers,
                                      ImmutableList<IQTreeTransformer> postTransformers,
                                      IntermediateQueryFactory iqFactory) {
        this.preTransformers = preTransformers;
        this.postTransformers = postTransformers;
        this.childTransformer = new ChildTransformer(iqFactory, this);
    }

    @Override
    public IQTree transform(IQTree initialTree) {
        //Non-final
        IQTree currentTree = initialTree;

        for (IQTreeTransformer transformer : preTransformers) {
            currentTree = transformer.transform(currentTree);
        }

        currentTree = childTransformer.transform(currentTree);

        for (IQTreeTransformer transformer : postTransformers) {
            currentTree = transformer.transform(currentTree);
        }

        return currentTree;
    }
}
