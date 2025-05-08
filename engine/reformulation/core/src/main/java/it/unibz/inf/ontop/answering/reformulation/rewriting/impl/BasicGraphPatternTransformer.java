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
import it.unibz.inf.ontop.model.term.ImmutableExpression;

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

        return formInnerJoin(builderChildren.build(), rootNode.getOptionalFilterCondition());
    }

    @Override
    public IQTree transformIntensionalData(IntensionalDataNode intensionalDataNode) {
        return formInnerJoin(transformBGP(ImmutableList.of(intensionalDataNode)), Optional.empty());
    }

    private IQTree formInnerJoin(ImmutableList<IQTree> list, Optional<ImmutableExpression> filter) {
        switch (list.size()) {
            case 0:
                throw new IllegalStateException("All triple patterns of BGP have been eliminated by the transformation");
            case 1:
                return iqTreeTools.createOptionalUnaryIQTree(
                        filter.map(iqFactory::createFilterNode),
                        list.get(0));
            default:
                return iqFactory.createNaryIQTree(iqFactory.createInnerJoinNode(filter), list);
        }
    }

    private void addTransformedBGP(ImmutableList.Builder<IQTree> builderChildren, ImmutableList<IntensionalDataNode> currentBGP) {
        if (!currentBGP.isEmpty())
            builderChildren.addAll(transformBGP(currentBGP));
    }

    protected abstract ImmutableList<IQTree> transformBGP(ImmutableList<IntensionalDataNode> bgp);
}

