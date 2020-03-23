package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class IQ2CQ {

    public static ImmutableList<DataAtom<RelationPredicate>> toDataAtoms(ImmutableList<ExtensionalDataNode> nodes,
                                                                         CoreSingletons singletons) {
        AtomFactory atomFactory = singletons.getAtomFactory();
        VariableGenerator variableGenerator = singletons.getCoreUtilsFactory().createVariableGenerator(
                nodes.stream()
                        .flatMap(a -> a.getVariables().stream())
                        .collect(ImmutableCollectors.toSet()));
         return nodes.stream()
                 .map(node -> toDataAtom(node, variableGenerator, atomFactory))
                 .collect(ImmutableCollectors.toList());
    }

    private static DataAtom<RelationPredicate> toDataAtom(ExtensionalDataNode node, VariableGenerator variableGenerator,
                                                          AtomFactory atomFactory) {
        ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap = node.getArgumentMap();
        RelationPredicate predicate = node.getRelationDefinition().getAtomPredicate();
        ImmutableList<VariableOrGroundTerm> newArguments = IntStream.range(0, predicate.getArity())
                .boxed()
                .map(i -> Optional.ofNullable(argumentMap.get(i))
                        .map(t -> (VariableOrGroundTerm) t)
                        .orElseGet(variableGenerator::generateNewVariable))
                .collect(ImmutableCollectors.toList());

        return atomFactory.getDataAtom(predicate, newArguments);
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

    //  assumes FilterAbsorber has been applied
    public static Optional<ImmutableList<ExtensionalDataNode>> getExtensionalDataNodes(IQTree tree) {
        QueryNode node = tree.getRootNode();
        if (node instanceof FilterNode) {
            // unguarded type cast - see FilterAbsorber
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
            return ((FilterNode)tree.getRootNode()).getOptionalFilterCondition().get().flattenAND()
                    .collect(ImmutableCollectors.toSet());
        }
        else if (node instanceof ExtensionalDataNode) {
            return ImmutableSet.of();
        }
        else if (node instanceof TrueNode) {
            return ImmutableSet.of();
        }
        else if (node instanceof InnerJoinNode) {
            return ((InnerJoinNode)tree.getRootNode()).getOptionalFilterCondition()
                    .map(e -> e.flattenAND()
                            .collect(ImmutableCollectors.toSet()))
                    .orElseGet(ImmutableSet::of);
        }
        throw new IllegalStateException("Use getExtensionalDataNodes first to check whether it's a CQ");
    }

    public static Optional<ImmutableExpression> getConjunction(Optional<ImmutableExpression> optExpression, List<ImmutableExpression> expressions, TermFactory termFactory) {
        if (expressions.isEmpty())
            throw new IllegalArgumentException("Nonempty list of filters expected");

        ImmutableExpression result = (optExpression.isPresent()
                ? Stream.concat(Stream.of(optExpression.get()), expressions.stream())
                : expressions.stream())
                .reduce(null,
                        (a, b) -> (a == null) ? b : termFactory.getConjunction(a, b));
        return Optional.of(result);
    }

}
