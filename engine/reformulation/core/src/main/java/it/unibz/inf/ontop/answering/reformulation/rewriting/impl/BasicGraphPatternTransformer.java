package it.unibz.inf.ontop.answering.reformulation.rewriting.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;

public abstract class BasicGraphPatternTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

    @Inject
    protected BasicGraphPatternTransformer(IntermediateQueryFactory iqFactory) {
        super(iqFactory);
    }

    @Override
    public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        ImmutableList.Builder<IntensionalDataNode> builderBGP = ImmutableList.builder();
        ImmutableList.Builder<IQTree> builderChildren = ImmutableList.builder();
        for (IQTree child : children) {
            if (child.getRootNode() instanceof IntensionalDataNode) {
                builderBGP.add((IntensionalDataNode)child);
            }
            else {
                ImmutableList<IntensionalDataNode> currentBGP = builderBGP.build();
                if (!currentBGP.isEmpty())
                    builderChildren.addAll(transformBGP(currentBGP));
                builderChildren.add(child.acceptTransformer(this));
            }
        }
        ImmutableList<IntensionalDataNode> currentBGP = builderBGP.build();
        if (!currentBGP.isEmpty())
            builderChildren.addAll(transformBGP(currentBGP));

        ImmutableList<IQTree> result = builderChildren.build();
        switch (result.size()) {
            case 0:
                throw new IllegalStateException("All triple patterns of BGP have been eliminated by Sigma-LIDs");
            case 1:
                if (rootNode.getOptionalFilterCondition().isPresent())
                    return iqFactory.createUnaryIQTree(
                            iqFactory.createFilterNode(rootNode.getOptionalFilterCondition().get()),
                            result.get(0));
                else return result.get(0);
            default:
                return iqFactory.createNaryIQTree(rootNode, result);
        }
    }

    protected abstract ImmutableList<IntensionalDataNode> transformBGP(ImmutableList<IntensionalDataNode> triplePatterns);
}
