package it.unibz.inf.ontop.answering.reformulation.rewriting.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTransformer;

public abstract class BasicGraphPatternTransformer extends DefaultRecursiveIQTransformer {

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
                builderChildren.add(child);
            }
        }
        ImmutableList<IntensionalDataNode> currentBGP = builderBGP.build();
        if (!currentBGP.isEmpty())
            builderChildren.addAll(transformBGP(currentBGP));

        return iqFactory.createNaryIQTree(rootNode, builderChildren.build());
    }

    protected abstract ImmutableList<IntensionalDataNode> transformBGP(ImmutableList<IntensionalDataNode> triplePatterns);
}
