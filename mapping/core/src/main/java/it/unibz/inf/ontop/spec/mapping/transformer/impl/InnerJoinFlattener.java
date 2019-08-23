package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.impl.LazyRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.AbstractMap;
import java.util.Optional;
import java.util.stream.Stream;

/*
  transforms InnerJoin(A, InnerJoin(B, C)) into InnerJoin(A, B, C) (with all combinations)
*/

public class InnerJoinFlattener {

    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;

    public InnerJoinFlattener(IntermediateQueryFactory iqFactory, TermFactory termFactory) {
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
    }

    public IQTree apply(IQTree tree) {
        return tree.acceptTransformer(new LazyRecursiveIQTreeVisitingTransformer(iqFactory) {
            @Override
            public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
                ImmutableList<AbstractMap.SimpleImmutableEntry<IQTree, IQTree>> childrenReplacement = transformChildren(children);
                ImmutableList<IQTree> newChildren = extractChildren(childrenReplacement);

                ImmutableList<IQTree> joinChildren = newChildren.stream()
                        .filter(c -> c.getRootNode() instanceof InnerJoinNode)
                        .collect(ImmutableCollectors.toList());

                if (!joinChildren.isEmpty()) {
                    ImmutableList<ImmutableExpression> filters = joinChildren.stream()
                            .map(c -> ((InnerJoinNode) c.getRootNode()).getOptionalFilterCondition())
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(ImmutableCollectors.toList());

                    return iqFactory.createNaryIQTree(
                            filters.isEmpty()
                                    ? rootNode
                                    : iqFactory.createInnerJoinNode(IQ2CQ.getConjunction(
                                            rootNode.getOptionalFilterCondition(),
                                            filters, termFactory)),
                            Stream.concat(
                                    newChildren.stream().filter(c -> !(c.getRootNode() instanceof InnerJoinNode)),
                                    joinChildren.stream().flatMap(c -> c.getChildren().stream()))
                                    .collect(ImmutableCollectors.toList()));
                }

                return childrenReplacement.stream().allMatch(e -> e.getKey() == e.getValue())
                        ? tree
                        : iqFactory.createNaryIQTree(rootNode, newChildren);
            }
        });
    }

}
