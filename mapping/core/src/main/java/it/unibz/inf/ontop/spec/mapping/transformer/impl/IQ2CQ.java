package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;

public class IQ2CQ {

    public static ImmutableList<DataAtom<RelationPredicate>> toDataAtoms(ImmutableList<? extends IQTree> nodes) {
         return nodes.stream()
                .map(n -> ((DataNode<RelationPredicate>) n.getRootNode()).getProjectionAtom())
                .collect(ImmutableCollectors.toList());
    }

    public static IQTree toIQTree(ImmutableList<? extends IQTree> extensionalNodes, Optional<ImmutableExpression> joiningConditions, IntermediateQueryFactory iqFactory) {
        switch (extensionalNodes.size()) {
            case 0:
                return iqFactory.createTrueNode();
            case 1:
                return (joiningConditions.isPresent()
                        ? iqFactory.createUnaryIQTree(iqFactory.createFilterNode(joiningConditions.get()), extensionalNodes.get(0))
                        : extensionalNodes.get(0));
            default:
                return iqFactory.createNaryIQTree(
                        iqFactory.createInnerJoinNode(joiningConditions),
                        extensionalNodes.stream().collect(ImmutableCollectors.toList()));
        }
    }

    public static Optional<ImmutableList<ExtensionalDataNode>> getExtensionalDataNodes(IQTree tree) {
        QueryNode node = tree.getRootNode();
        if (node instanceof FilterNode) {
            // CAREFUL WITH TYPE CAST - ASSUMES FilterChildNormalizer has been applied
            if (!(tree.getChildren().get(0) instanceof ExtensionalDataNode))
                System.out.println("PANICCC: " + tree);
            return Optional.of(ImmutableList.of((ExtensionalDataNode)tree.getChildren().get(0)));
        }
        else if (node instanceof ExtensionalDataNode) {
            return Optional.of(ImmutableList.of((ExtensionalDataNode)tree));
        }
        else if (node instanceof TrueNode) {
            return Optional.of(ImmutableList.of());
        }
        else if (node instanceof InnerJoinNode) {
            if (tree.getChildren().stream().anyMatch(c -> !(c.getRootNode() instanceof ExtensionalDataNode)))
                return Optional.empty();

            return Optional.of(tree.getChildren().stream()
                    .map(n -> (ExtensionalDataNode)n)
                    .collect(ImmutableCollectors.toList()));
        }
        return Optional.empty();
    }

    public static ImmutableSet<ImmutableExpression> getFilterExpressions(IQTree tree) {
        QueryNode node = tree.getRootNode();
        if (node instanceof FilterNode) {
            return ((FilterNode)tree.getRootNode()).getOptionalFilterCondition().get().flattenAND();
        }
        else if (node instanceof ExtensionalDataNode) {
            return ImmutableSet.of();
        }
        else if (node instanceof TrueNode) {
            return ImmutableSet.of();
        }
        else if (node instanceof InnerJoinNode) {
            return ((InnerJoinNode)tree.getRootNode()).getOptionalFilterCondition().map(e -> e.flattenAND()).orElseGet(ImmutableSet::of);
        }
        throw new IllegalStateException("Use getExtensionalDataNodes first to check whether it's a CQ");
    }

}
