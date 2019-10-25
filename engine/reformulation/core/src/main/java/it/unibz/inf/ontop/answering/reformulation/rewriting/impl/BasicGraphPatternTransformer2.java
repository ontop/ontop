package it.unibz.inf.ontop.answering.reformulation.rewriting.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

public abstract class BasicGraphPatternTransformer2 extends DefaultRecursiveIQTreeVisitingTransformer {

    @Inject
    protected BasicGraphPatternTransformer2(IntermediateQueryFactory iqFactory) {
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
                add(builderChildren, builderBGP);
                builderChildren.add(child.acceptTransformer(this));
            }
        }
        add(builderChildren, builderBGP);

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

    private void add(ImmutableList.Builder<IQTree> builderChildren, ImmutableList.Builder<IntensionalDataNode> builderBGP) {
        ImmutableList<IntensionalDataNode> currentBGP = builderBGP.build();
        if (currentBGP.isEmpty())
            return;

        IQTree result = convertUCQ(transformBGP(currentBGP));
        if (result.getRootNode() instanceof InnerJoinNode &&
                !((InnerJoinNode)result.getRootNode()).getOptionalFilterCondition().isPresent()) {
                // flatten the join out - needed in sparql-compliance
                builderChildren.addAll(result.getChildren());
        }
        else
            builderChildren.add(result);
    }

    @Override
    public IQTree transformIntensionalData(IntensionalDataNode intensionalDataNode) {
        return convertUCQ(transformBGP(ImmutableList.of(intensionalDataNode)));
    }

    private IQTree convertUCQ(ImmutableList<IQTree> ucq) {
        if (ucq.size() == 1)
            return ucq.get(0);

        // intersection
        ImmutableSet<Variable> vars = ucq.get(0).getVariables().stream()
                .filter(v -> ucq.stream().allMatch(cq -> cq.getVariables().contains(v)))
                .collect(ImmutableCollectors.toSet());

        return iqFactory.createNaryIQTree(iqFactory.createUnionNode(vars), ucq);
    }

    protected abstract ImmutableList<IQTree> transformBGP(ImmutableList<IntensionalDataNode> triplePatterns);
}

