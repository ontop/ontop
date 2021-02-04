package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.injection.OptimizationSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.DistinctNode;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;

/**
 * TODO: find a better name
 */
class LookForDistinctTransformerImpl extends DefaultRecursiveIQTreeVisitingTransformer {
    private final IQTreeTransformer subTransformer;

    public LookForDistinctTransformerImpl(CardinalityFreeTransformerConstructor transformerConstructor,
                                          OptimizationSingletons optimizationSingletons) {
        super(optimizationSingletons.getCoreSingletons());
        this.subTransformer = transformerConstructor.create(this, optimizationSingletons);
    }

    @Override
    public IQTree transformDistinct(IQTree tree, DistinctNode rootNode, IQTree child) {
        IQTree newChild = subTransformer.transform(child);
        return (newChild.equals(child))
                ? tree
                : iqFactory.createUnaryIQTree(rootNode, newChild);
    }

    @FunctionalInterface
    interface CardinalityFreeTransformerConstructor {
        IQTreeTransformer create(IQTreeTransformer parentTransformer, OptimizationSingletons optimizationSingletons);
    }
}
