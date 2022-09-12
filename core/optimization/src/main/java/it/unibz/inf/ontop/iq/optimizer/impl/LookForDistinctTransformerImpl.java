package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.injection.CoreSingletons;
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
    private final CardinalityFreeTransformerConstructor2 transformerConstructor3;

    public LookForDistinctTransformerImpl(CardinalityFreeTransformerConstructor1 transformerConstructor,
                                          CoreSingletons coreSingletons) {
        super(coreSingletons);
        this.subTransformer = transformerConstructor.create(this);
        this.transformerConstructor3 = null;
    }

    public LookForDistinctTransformerImpl(CardinalityFreeTransformerConstructor2 transformerConstructor3,
                                          CoreSingletons coreSingletons) {
        super(coreSingletons);
        this.transformerConstructor3 = transformerConstructor3;
        this.subTransformer = null;
    }

    private IQTreeTransformer getSubTransformer(IQTree child) {
        return subTransformer == null
                ? transformerConstructor3.create(child, this)
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
    public interface CardinalityFreeTransformerConstructor1 {
        IQTreeTransformer create(IQTreeTransformer parentTransformer);
    }

    @FunctionalInterface
    public interface CardinalityFreeTransformerConstructor2 {
        IQTreeTransformer create(IQTree childOfDistinct, IQTreeTransformer parentTransformer);
    }
}
