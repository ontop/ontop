package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.injection.OptimizationSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.DistinctNode;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;

import javax.annotation.Nullable;

/**
 * TODO: find a better name
 */
public class LookForDistinctTransformerImpl extends DefaultRecursiveIQTreeVisitingTransformer {


    @Nullable
    private final IQTreeTransformer subTransformer;
    @Nullable
    private final CardinalityFreeTransformerConstructor3 transformerConstructor3;
    private final OptimizationSingletons optimizationSingletons;

    public LookForDistinctTransformerImpl(CardinalityFreeTransformerConstructor2 transformerConstructor,
                                          OptimizationSingletons optimizationSingletons) {
        super(optimizationSingletons.getCoreSingletons());
        this.subTransformer = transformerConstructor.create(this, optimizationSingletons);
        this.optimizationSingletons = optimizationSingletons;
        this.transformerConstructor3 = null;
    }

    public LookForDistinctTransformerImpl(CardinalityFreeTransformerConstructor3 transformerConstructor3,
                                          OptimizationSingletons optimizationSingletons) {
        super(optimizationSingletons.getCoreSingletons());
        this.transformerConstructor3 = transformerConstructor3;
        this.optimizationSingletons = optimizationSingletons;
        this.subTransformer = null;
    }

    private IQTreeTransformer getSubTransformer(IQTree child) {
        return subTransformer == null
                ? transformerConstructor3.create(child, this, optimizationSingletons)
                : subTransformer;
    }

    @Override
    public IQTree transformDistinct(IQTree tree, DistinctNode rootNode, IQTree child) {
        IQTree newChild = getSubTransformer(child).transform(child);
        return (newChild.equals(child))
                ? tree
                : iqFactory.createUnaryIQTree(rootNode, newChild);
    }

    @FunctionalInterface
    public interface CardinalityFreeTransformerConstructor2 {
        IQTreeTransformer create(IQTreeTransformer parentTransformer, OptimizationSingletons optimizationSingletons);
    }

    @FunctionalInterface
    public interface CardinalityFreeTransformerConstructor3 {
        IQTreeTransformer create(IQTree childOfDistinct, IQTreeTransformer parentTransformer,
                                 OptimizationSingletons optimizationSingletons);
    }
}
