package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;

public class DefaultRecursiveIQTreeVisitingInnerJoinTransformer extends DefaultRecursiveIQTreeVisitingTransformer {
    private final InnerJoinTransformer transformer;

    DefaultRecursiveIQTreeVisitingInnerJoinTransformer(IntermediateQueryFactory iqFactory, InnerJoinTransformer transformer) {
        super(iqFactory);
        this.transformer = transformer;
    }

    @Override
    public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        // Recursive
        ImmutableList<IQTree> liftedChildren = NaryIQTreeTools.transformChildren(children, this::transformChild);

        return transformer.transformInnerJoin(tree, rootNode, liftedChildren)
                .orElseGet(() -> withTransformedChildren(tree, liftedChildren));
    }
}
