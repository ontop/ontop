package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.LazyRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.AbstractMap;
import java.util.Optional;



/*
 PINCHED FROM ExplicitEqualityTransformerImpl and simplified (no filter pull-up from LeftJoin)

 */

public class FilterAbsorber {

    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;

    public FilterAbsorber(IntermediateQueryFactory iqFactory, TermFactory termFactory) {
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
    }

    public IQTree apply(IQTree tree) {
        return tree.acceptTransformer(new LazyRecursiveIQTreeVisitingTransformer(iqFactory) {

            /*
                    LeftJoin(L, Filter(FR, R); F) -> LeftJoin(L, R; F AND FR)
             */

            @Override
            public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
                IQTree newLeftChild = leftChild.acceptTransformer(this), newRightChild = rightChild.acceptTransformer(this);

                if (newRightChild.getRootNode() instanceof FilterNode) {
                    LeftJoinNode newLeftJoinNode = iqFactory.createLeftJoinNode(IQ2CQ.getConjunction(
                            rootNode.getOptionalFilterCondition(),
                            ImmutableList.of(((FilterNode)newRightChild.getRootNode()).getFilterCondition()), termFactory));

                    return iqFactory.createBinaryNonCommutativeIQTree(newLeftJoinNode, newLeftChild,
                            ((UnaryIQTree)newRightChild).getChild());
                }

                return (leftChild == newLeftChild) && (rightChild == newRightChild)
                        ? tree
                        : iqFactory.createBinaryNonCommutativeIQTree(rootNode, newLeftChild, newRightChild);
            }

            /*
                    InnerJoin(Filter(F1, R1),...,Filter(Fn, Rn); F) -> InnerJoin(R1,...,Rn; F AND F1 AND ... AND Fn)
            */

            @Override
            public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
                ImmutableList<AbstractMap.SimpleImmutableEntry<IQTree, IQTree>> childrenReplacement = transformChildren(children);
                ImmutableList<IQTree> newChildren = extractChildren(childrenReplacement);
                ImmutableList<ImmutableExpression> filterChildExpressions = newChildren.stream()
                        .filter(t -> t.getRootNode() instanceof FilterNode)
                        .map(t -> ((FilterNode) t.getRootNode()).getFilterCondition())
                        .collect(ImmutableCollectors.toList());

                if (!filterChildExpressions.isEmpty()) {
                    return iqFactory.createNaryIQTree(
                            iqFactory.createInnerJoinNode(IQ2CQ.getConjunction(
                                    rootNode.getOptionalFilterCondition(),
                                    filterChildExpressions,
                                    termFactory)),
                            newChildren.stream()
                                    .map(t -> t.getRootNode() instanceof FilterNode
                                            ? ((UnaryIQTree) t).getChild()
                                            : t)
                                    .collect(ImmutableCollectors.toList()));
                }
                return childrenReplacement.stream().allMatch(e -> e.getKey() == e.getValue())
                        ? tree
                        : iqFactory.createNaryIQTree(rootNode, newChildren);
            }

            /*
                    Filter(F1, Filter(F2, R)) -> Filter(F1 AND F2, R)
                    Filter(F1, InnerJoin(R1,...,Rn; F)) -> InnerJoin(R1,...,Rn; F AND F1)
             */

            @Override
            public IQTree transformFilter(IQTree tree, FilterNode rootNode, IQTree child) {
                IQTree newChild = child.acceptTransformer(this);

                if (newChild.getRootNode() instanceof FilterNode) {
                    return iqFactory.createUnaryIQTree(
                            iqFactory.createFilterNode(IQ2CQ.getConjunction(
                                    Optional.of(rootNode.getFilterCondition()),
                                    ImmutableList.of(((FilterNode) newChild.getRootNode()).getFilterCondition()), termFactory).get()),
                            ((UnaryIQTree)newChild).getChild());
                }
                else if (newChild.getRootNode() instanceof InnerJoinNode) {
                    return iqFactory.createNaryIQTree(
                            iqFactory.createInnerJoinNode(IQ2CQ.getConjunction(
                                ((InnerJoinNode)newChild.getRootNode()).getOptionalFilterCondition(),
                                ImmutableList.of(rootNode.getFilterCondition()), termFactory)),
                            newChild.getChildren());
                }
                return (child == newChild) ? tree : iqFactory.createUnaryIQTree(rootNode, newChild);
            }
        });
    }
}
