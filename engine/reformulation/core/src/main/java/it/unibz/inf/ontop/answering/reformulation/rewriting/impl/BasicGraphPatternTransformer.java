package it.unibz.inf.ontop.answering.reformulation.rewriting.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;

import java.util.Optional;

public abstract class BasicGraphPatternTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

    private final IQTreeTools iqTreeTools;

    @Inject
    protected BasicGraphPatternTransformer(IntermediateQueryFactory iqFactory, IQTreeTools iqTreeTools) {
        super(iqFactory);
        this.iqTreeTools = iqTreeTools;
    }

    @Override
    public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        ImmutableList.Builder<IntensionalDataNode> builderBGP = ImmutableList.builder();
        ImmutableList.Builder<IQTree> builderChildren = ImmutableList.builder();
        for (IQTree child : children) {
            if (child.getRootNode() instanceof IntensionalDataNode) {
                builderBGP.add((IntensionalDataNode)child);
            }
            else {
                addTransformedBGP(builderChildren, builderBGP.build());
                builderBGP = ImmutableList.builder();
                builderChildren.add(child.acceptTransformer(this));
            }
        }
        addTransformedBGP(builderChildren, builderBGP.build());

        ImmutableList<IQTree> list = builderChildren.build();
        return iqTreeTools.createOptionalInnerJoinTree(rootNode.getOptionalFilterCondition(), list)
                .orElseThrow(() -> new IllegalStateException("All triple patterns of BGP have been eliminated by the transformation"));
    }

    @Override
    public IQTree transformIntensionalData(IntensionalDataNode intensionalDataNode) {
        ImmutableList<IQTree> list = transformBGP(ImmutableList.of(intensionalDataNode));
        return iqTreeTools.createOptionalInnerJoinTree(Optional.empty(), list)
                .orElseThrow(() -> new IllegalStateException("All triple patterns of BGP have been eliminated by the transformation"));
    }

    private void addTransformedBGP(ImmutableList.Builder<IQTree> builderChildren, ImmutableList<IntensionalDataNode> currentBGP) {
        if (!currentBGP.isEmpty())
            builderChildren.addAll(transformBGP(currentBGP));
    }

    protected abstract ImmutableList<IQTree> transformBGP(ImmutableList<IntensionalDataNode> bgp);
}

