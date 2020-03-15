package it.unibz.inf.ontop.iq.executor.join;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.tools.VariableOccurrenceAnalyzer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.tools.impl.NaiveVariableOccurrenceAnalyzerImpl;
import it.unibz.inf.ontop.iq.impl.QueryTreeComponent;
import it.unibz.inf.ontop.iq.proposal.InnerJoinOptimizationProposal;
import it.unibz.inf.ontop.iq.exception.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.iq.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.iq.proposal.impl.NodeCentricOptimizationResultsImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Use foreign keys to remove some redundant inner joins
 *
 * Normalization assumption: variables are reused between data nodes (no explicit equality between variables)
 *
 */
public class RedundantJoinFKExecutor implements InnerJoinExecutor {

    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;

    @Inject
    private RedundantJoinFKExecutor(IntermediateQueryFactory iqFactory, TermFactory termFactory) {
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
    }

    @Override
    public NodeCentricOptimizationResults<InnerJoinNode> apply(InnerJoinOptimizationProposal proposal,
                                                               IntermediateQuery query,
                                                               QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException {

        InnerJoinNode joinNode = proposal.getFocusNode();
        ImmutableMultimap<RelationDefinition, ExtensionalDataNode> dataNodeMap = extractDataNodeMap(query, joinNode);

        ImmutableList<Redundancy> redundancies = findRedundancies(query, joinNode, dataNodeMap);

        ImmutableSet<ExtensionalDataNode> nodesToRemove = redundancies.stream()
                .map(r -> r.dataNode)
                .collect(ImmutableCollectors.toSet());

        ImmutableSet<Variable> variablesToRequireNonNull = redundancies.stream()
                .flatMap(r -> r.fkVariables.stream())
                .collect(ImmutableCollectors.toSet());

        if (!nodesToRemove.isEmpty()) {

            NodeCentricOptimizationResults<InnerJoinNode> result = applyOptimization(query, treeComponent,
                    joinNode, nodesToRemove, variablesToRequireNonNull);

            return result;
        }
        /*
         * No change
         */
        else {
            return new NodeCentricOptimizationResultsImpl<>(query, joinNode);
        }
    }

    private NodeCentricOptimizationResults<InnerJoinNode> applyOptimization(IntermediateQuery query,
                                                                            QueryTreeComponent treeComponent,
                                                                            InnerJoinNode joinNode,
                                                                            ImmutableSet<ExtensionalDataNode> nodesToRemove,
                                                                            ImmutableSet<Variable> variablesToRequireNonNull) {

        /*
         * First removes all the redundant nodes
         */
        nodesToRemove
                .forEach(treeComponent::removeSubTree);

        Optional<ImmutableExpression> newCondition = joinNode.getOptionalFilterCondition()
                .map(c -> termFactory.getConjunction(Stream.concat(
                        c.flattenAND(),
                        variablesToRequireNonNull.stream()
                                .map(termFactory::getDBIsNotNull))))
                .orElseGet(() -> termFactory.getConjunction(
                        variablesToRequireNonNull.stream()
                                .map(termFactory::getDBIsNotNull)));

        /*
         * Then replaces the join node if needed
         */
        switch (query.getChildren(joinNode).size()) {
            case 0:
                throw new IllegalStateException("Redundant join elimination should not eliminate all the children");
            case 1:
                QueryNode replacingChild = query.getFirstChild(joinNode).get();

                if (newCondition.isPresent()) {
                    FilterNode newFilterNode = iqFactory.createFilterNode(newCondition.get());
                    treeComponent.replaceNode(joinNode, newFilterNode);
                    /*
                     * NB: the filter node is not declared as the replacing node but the child is.
                     * Why? Because a JOIN with a filtering condition could decomposed into two different nodes.
                     */
                }
                else {
                    treeComponent.replaceNodeByChild(joinNode, Optional.empty());
                }
                return new NodeCentricOptimizationResultsImpl<>(query, Optional.of(replacingChild));

            default:
                InnerJoinNode newJoinNode = iqFactory.createInnerJoinNode(newCondition);
                treeComponent.replaceNode(joinNode, newJoinNode);
                return new NodeCentricOptimizationResultsImpl<>(query, newJoinNode);
        }
    }

    /**
     * Predicates not having a DatabaseRelationDefinition are ignored
     */
    private ImmutableMultimap<RelationDefinition, ExtensionalDataNode> extractDataNodeMap(IntermediateQuery query,
                                                                                          InnerJoinNode joinNode) {
        return query.getChildren(joinNode).stream()
                .filter(c -> c instanceof ExtensionalDataNode)
                .map(c -> (ExtensionalDataNode) c)
                .map(c -> Maps.immutableEntry(c.getRelationDefinition(), c))
                .collect(ImmutableCollectors.toMultimap());
    }

    private ImmutableList<Redundancy> findRedundancies(IntermediateQuery query, InnerJoinNode joinNode,
                                                    ImmutableMultimap<RelationDefinition, ExtensionalDataNode> dataNodeMap) {
        return dataNodeMap.keySet().stream()
                .flatMap(r -> r.getForeignKeys().stream()
                        .flatMap(c -> selectRedundantNodesForConstraint(r, c, query, joinNode, dataNodeMap)))
                .collect(ImmutableCollectors.toList());
    }

    /**
     * TODO: explain
     */
    private Stream<Redundancy> selectRedundantNodesForConstraint(RelationDefinition sourceRelation,
                                                               ForeignKeyConstraint constraint,
                                                               IntermediateQuery query,
                                                               InnerJoinNode joinNode,
                                                               ImmutableMultimap<RelationDefinition, ExtensionalDataNode> dataNodeMap) {
        /*
         * "Target" data nodes === "referenced" data nodes
         */
        ImmutableCollection<ExtensionalDataNode> targetDataNodes = dataNodeMap.get(constraint.getReferencedRelation());

        /*
         * No optimization possible
         */
        if (targetDataNodes.isEmpty()) {
            return Stream.empty();
        }

        return dataNodeMap.get(sourceRelation).stream()
                .flatMap(s -> targetDataNodes.stream()
                        .filter(t -> areMatching(s,t,constraint)))
                .distinct()
                .filter(t -> areNonFKColumnsUnused(t, query, constraint))
                .map(t -> new Redundancy(t, extractJoiningVariables(t, constraint)));
    }

    /**
     *
     * TODO: explain
     */
    private boolean areMatching(ExtensionalDataNode sourceDataNode, ExtensionalDataNode targetDataNode, ForeignKeyConstraint constraint) {

        ImmutableMap<Integer, ? extends VariableOrGroundTerm> sourceArgumentMap = sourceDataNode.getArgumentMap();
        ImmutableMap<Integer, ? extends VariableOrGroundTerm> targetArgumentMap = targetDataNode.getArgumentMap();

        return constraint.getComponents().stream()
                .allMatch(c -> sourceArgumentMap.get(c.getAttribute().getIndex() - 1)
                        .equals(targetArgumentMap.get(c.getReference().getIndex() - 1)));
    }

    /**
     * TODO: explain
     */
    private boolean areNonFKColumnsUnused(ExtensionalDataNode targetDataNode, IntermediateQuery query,
                                          ForeignKeyConstraint constraint) {

        ImmutableMap<Integer, ? extends VariableOrGroundTerm> targetArguments = targetDataNode.getArgumentMap();

        ImmutableSet<Integer> fkTargetIndexes = constraint.getComponents().stream()
                .map(c -> c.getReference().getIndex() - 1)
                .collect(ImmutableCollectors.toSet());

        /*
         * Terms appearing in non-FK positions
         */
        ImmutableList<VariableOrGroundTerm> remainingTerms = IntStream.range(0, targetArguments.size())
                .filter(i -> !fkTargetIndexes.contains(i))
                .boxed()
                .map(targetArguments::get)
                .collect(ImmutableCollectors.toList());

        /*
         * Check usage in the data atom.
         *
         * 1 - They should all variables
         * 2 - They should be no duplicate
         * 3 - They must be distinct from the FK target terms
         */
        if ((!remainingTerms.stream().allMatch(t -> t instanceof Variable))
            || (ImmutableSet.copyOf(remainingTerms).size() < remainingTerms.size())
            || fkTargetIndexes.stream()
                .map(targetArguments::get)
                .anyMatch(remainingTerms::contains))
            return false;

        /*
         * Check that the remaining variables are not used anywhere else
         */
        // TODO: use a more efficient implementation
        VariableOccurrenceAnalyzer analyzer = new NaiveVariableOccurrenceAnalyzerImpl();

        return remainingTerms.stream()
                .map(v -> (Variable) v)
                .noneMatch(v -> analyzer.isVariableUsedSomewhereElse(query, targetDataNode, v));
    }

    private ImmutableSet<Variable> extractJoiningVariables(ExtensionalDataNode node, ForeignKeyConstraint constraint) {
        ImmutableMap<Integer, ? extends VariableOrGroundTerm> targetArgumentMap = node.getArgumentMap();

        return constraint.getComponents().stream()
                .map(c -> targetArgumentMap.get(c.getReference().getIndex() - 1))
                .filter(t -> t instanceof Variable)
                .map(t -> (Variable) t)
                .collect(ImmutableCollectors.toSet());
    }

    private static class Redundancy {
        private final ExtensionalDataNode dataNode;
        private final ImmutableSet<Variable> fkVariables;

        private Redundancy(ExtensionalDataNode dataNode, ImmutableSet<Variable> fkVariables) {
            this.dataNode = dataNode;
            this.fkVariables = fkVariables;
        }
    }


}
