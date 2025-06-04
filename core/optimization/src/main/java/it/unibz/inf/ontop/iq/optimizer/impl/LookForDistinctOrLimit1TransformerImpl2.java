package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.DistinctNode;
import it.unibz.inf.ontop.iq.node.SliceNode;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.impl.IQTreeTransformerAdapter;

import java.util.function.BiFunction;

public class LookForDistinctOrLimit1TransformerImpl2 extends DefaultRecursiveIQTreeVisitingTransformer {

    private final BiFunction<IQTree, IQTreeTransformer, IQTreeTransformer> transformerConstructor2;

    public LookForDistinctOrLimit1TransformerImpl2(BiFunction<IQTree, IQTreeTransformer, IQTreeTransformer> transformerConstructor2,
                                                   IntermediateQueryFactory iqFactory) {
        super(iqFactory);
        this.transformerConstructor2 = transformerConstructor2;
    }

    private IQTreeTransformer getSubTransformer(IQTree child) {
        return transformerConstructor2.apply(child, new IQTreeTransformerAdapter(this));
    }

    @Override
    public IQTree transformDistinct(UnaryIQTree tree, DistinctNode rootNode, IQTree child) {
        IQTree newChild = getSubTransformer(child).transform(child);
        return newChild.equals(child)
                ? tree
                : iqFactory.createUnaryIQTree(rootNode, newChild);
    }

    @Override
    public IQTree transformSlice(UnaryIQTree tree, SliceNode sliceNode, IQTree child) {
        // LIMIT 1
        if (sliceNode.getOffset() == 0 && sliceNode.getLimit().filter(l -> l <= 1).isPresent()) {
            IQTree newChild = getSubTransformer(child).transform(child);
            return newChild.equals(child)
                    ? tree
                    : iqFactory.createUnaryIQTree(sliceNode, newChild);
        }
        return super.transformSlice(tree, sliceNode, child);
    }
}
