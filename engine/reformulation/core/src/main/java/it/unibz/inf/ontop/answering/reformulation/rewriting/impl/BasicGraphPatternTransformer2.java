package it.unibz.inf.ontop.answering.reformulation.rewriting.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.DataNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

public abstract class BasicGraphPatternTransformer2 extends DefaultRecursiveIQTreeVisitingTransformer {

    @Inject
    protected BasicGraphPatternTransformer2(IntermediateQueryFactory iqFactory) {
        super(iqFactory);
    }

    @Override
    public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        ImmutableList.Builder<DataNode<RDFAtomPredicate>> builderBGP = ImmutableList.builder();
        ImmutableList.Builder<IQTree> builderChildren = ImmutableList.builder();
        for (IQTree child : children) {
            if (child.getRootNode() instanceof IntensionalDataNode) {
                builderBGP.add((DataNode<RDFAtomPredicate>)child);
            }
            else {
                ImmutableList<DataNode<RDFAtomPredicate>> currentBGP = builderBGP.build();
                if (!currentBGP.isEmpty())
                    builderChildren.add(convertUCQ(transformBGP(currentBGP)));
                builderChildren.add(child.acceptTransformer(this));
            }
        }
        ImmutableList<DataNode<RDFAtomPredicate>> currentBGP = builderBGP.build();
        if (!currentBGP.isEmpty())
            builderChildren.add(convertUCQ(transformBGP(currentBGP)));

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

    @Override
    public IQTree transformIntensionalData(IntensionalDataNode intensionalDataNode) {
        return convertUCQ(transformBGP(ImmutableList.of((DataNode)intensionalDataNode)));
    }

    private IQTree convertUCQ(ImmutableList<IQTree> ucq) {
        if (ucq.size() == 1)
            return ucq.get(0);

        ImmutableSet<Variable> vars = ucq.stream()
                .flatMap(a -> a.getVariables().stream())
                .collect(ImmutableCollectors.toSet());

        return iqFactory.createNaryIQTree(iqFactory.createUnionNode(vars), ucq);
    }

    protected abstract ImmutableList<IQTree> transformBGP(ImmutableList<DataNode<RDFAtomPredicate>> triplePatterns);
}

