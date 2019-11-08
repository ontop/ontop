package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.OptimizationSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.DistinctNode;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.Variable;

/**
 * TODO: find a better name
 */
class LookForDistinctTransformerImpl extends DefaultRecursiveIQTreeVisitingTransformer {

    protected final OptimizationSingletons optimizationSingletons;
    private final CardinalityFreeTransformerConstructor transformerConstructor;

    public LookForDistinctTransformerImpl(CardinalityFreeTransformerConstructor transformerConstructor,
                                          OptimizationSingletons optimizationSingletons) {
        super(optimizationSingletons.getCoreSingletons());
        this.optimizationSingletons = optimizationSingletons;
        this.transformerConstructor = transformerConstructor;
    }

    @Override
    public IQTree transformDistinct(IQTree tree, DistinctNode rootNode, IQTree child) {
        IQTreeTransformer newTransformer = transformerConstructor.create(
                ImmutableSet.of(),
                this,
                optimizationSingletons);

        IQTree newChild = newTransformer.transform(child);
        return (newChild.equals(child))
                ? tree
                : iqFactory.createUnaryIQTree(rootNode, newChild);
    }

    @FunctionalInterface
    interface CardinalityFreeTransformerConstructor {
        IQTreeTransformer create(ImmutableSet<Variable> discardedVariables,
                                 IQTreeTransformer parentTransformer, OptimizationSingletons optimizationSingletons);
    }
}
