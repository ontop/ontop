package it.unibz.inf.ontop.iq.executor.join;

import com.google.common.collect.*;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition;
import it.unibz.inf.ontop.iq.impl.QueryTreeComponent;
import it.unibz.inf.ontop.iq.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.iq.proposal.SubstitutionPropagationProposal;
import it.unibz.inf.ontop.iq.proposal.impl.NodeCentricOptimizationResultsImpl;
import it.unibz.inf.ontop.iq.proposal.impl.SubstitutionPropagationProposalImpl;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.node.BinaryOrderedOperatorNode.ArgumentPosition.LEFT;

public class SelfJoinLikeExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelfJoinLikeExecutor.class);

    /**
     * TODO: explain
     *
     * TODO: find valid cases
     */
    protected static final class AtomUnificationException extends Exception {
    }


    /**
     * TODO: explain
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected static class PredicateLevelProposal {

        private final ImmutableCollection<ImmutableSubstitution<VariableOrGroundTerm>> substitutions;
        private final Optional<ImmutableExpression> isNotNullConjunction;

        public PredicateLevelProposal(ImmutableCollection<ImmutableSubstitution<VariableOrGroundTerm>> substitutions,
                                      Optional<ImmutableExpression> isNotNullConjunction) {
            this.substitutions = substitutions;
            this.isNotNullConjunction = isNotNullConjunction;
        }

        public ImmutableCollection<ImmutableSubstitution<VariableOrGroundTerm>> getSubstitutions() {
            return substitutions;
        }

        public Optional<ImmutableExpression> getIsNotNullConjunction() {
            return isNotNullConjunction;
        }
    }


    /**
     * TODO: explain
     */
    protected static class ConcreteProposal {

        private final Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalSubstitution;
        private final Optional<ImmutableExpression> optionalIsNotNullExpression;

        public ConcreteProposal(Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalSubstitution,
                                Optional<ImmutableExpression> optionalIsNotNullExpression) {
            this.optionalSubstitution = optionalSubstitution;
            this.optionalIsNotNullExpression = optionalIsNotNullExpression;
        }

        public Optional<ImmutableSubstitution<VariableOrGroundTerm>> getOptionalSubstitution() {
            return optionalSubstitution;
        }


        public Optional<ImmutableExpression> getOptionalIsNotNullExpression() {
            return optionalIsNotNullExpression;
        }
    }

    private static class ParentAndChildPosition {
        public final QueryNode parent;
        public final Optional<ArgumentPosition> position;

        private ParentAndChildPosition(QueryNode parent, Optional<ArgumentPosition> position) {
            this.parent = parent;
            this.position = position;
        }
    }


    private final ImmutableUnificationTools unificationTools;
    private final TermFactory termFactory;

    protected SelfJoinLikeExecutor(ImmutableUnificationTools unificationTools, TermFactory termFactory) {
        this.unificationTools = unificationTools;
        this.termFactory = termFactory;
    }


    protected static ImmutableMultimap<RelationDefinition, ExtensionalDataNode> extractDataNodes(ImmutableList<QueryNode> siblings) {
        ImmutableMultimap.Builder<RelationDefinition, ExtensionalDataNode> mapBuilder = ImmutableMultimap.builder();
        for (QueryNode node : siblings) {
            if (node instanceof ExtensionalDataNode) {
                ExtensionalDataNode dataNode = (ExtensionalDataNode) node;
                mapBuilder.put(dataNode.getRelationDefinition(), dataNode);
            }
        }
        return mapBuilder.build();
    }

    protected Optional<ImmutableSubstitution<VariableOrGroundTerm>> mergeSubstitutions(
            ImmutableList<ImmutableSubstitution<VariableOrGroundTerm>> substitutions,
            ImmutableMultimap<RelationDefinition, ExtensionalDataNode> initialDataNodeMap,
            ImmutableList<Variable> priorityVariables)
            throws AtomUnificationException {

        ImmutableMap<Variable, Collection<RelationDefinition>> occurrenceVariableMap = initialDataNodeMap.asMap().entrySet().stream()
                .flatMap(e -> e.getValue().stream()
                        .flatMap(n -> n.getVariables().stream())
                        .map(v -> Maps.immutableEntry(v, e.getKey())))
                .collect(ImmutableCollectors.toMultimap()).asMap();

        // Variables appearing for for more one relation predicate
        ImmutableSet<Variable> sharedVariables = occurrenceVariableMap.entrySet().stream()
                .filter(e -> ImmutableSet.copyOf(e.getValue()).size() > 1)
                .map(Map.Entry::getKey)
                .collect(ImmutableCollectors.toSet());
        ImmutableSet<Variable> nonSharedVariables = Sets.difference(occurrenceVariableMap.keySet(), sharedVariables)
                .immutableCopy();

        /*
         * For performance purposes, we can detach some fragments from the substitution to be "unified" with the following atom.
         */
        ImmutableList.Builder<ImmutableSubstitution<VariableOrGroundTerm>> nonSharedSubstitutionListBuilder = ImmutableList.builder();

        // Non-final
        Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalAccumulatedSubstitution = Optional.empty();

        for (ImmutableSubstitution<VariableOrGroundTerm> substitution : substitutions) {
            if (!substitution.isEmpty()) {
                if (optionalAccumulatedSubstitution.isPresent()) {

                    ImmutableSubstitution<VariableOrGroundTerm> accumulatedSubstitution = optionalAccumulatedSubstitution.get();

                    /*
                     * Before the following unification, we detach a fragment about non-shared variables from the accumulated substitution
                     *
                     * Particularly useful when dealing with tables with a large number of columns (e.g. views after collapsing some JSON objects)
                     *
                     */
                    ImmutableSubstitution<VariableOrGroundTerm> nonSharedSubstitution = accumulatedSubstitution.reduceDomainToIntersectionWith(nonSharedVariables);
                    if (!nonSharedSubstitution.isEmpty())
                        nonSharedSubstitutionListBuilder.add(nonSharedSubstitution);

                    ImmutableSubstitution<VariableOrGroundTerm> substitutionToUnify = nonSharedSubstitution.isEmpty()
                            ? accumulatedSubstitution
                            : accumulatedSubstitution.reduceDomainToIntersectionWith(sharedVariables);

                    Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalMGUS = unificationTools.computeAtomMGUS(
                            substitutionToUnify, substitution);
                    if (optionalMGUS.isPresent()) {
                        optionalAccumulatedSubstitution = optionalMGUS;
                    }
                    else {
                        // TODO: log a warning
                        throw new AtomUnificationException();
                    }
                }
                else {
                    optionalAccumulatedSubstitution = Optional.of(substitution);
                }
            }
        }

        return optionalAccumulatedSubstitution
                .map(s -> Stream.concat(
                            nonSharedSubstitutionListBuilder.build().stream(),
                            Stream.of(s))
                        .reduce((v1, v2) -> v2.composeWith2(v1))
                        .orElseThrow(() -> new MinorOntopInternalBugException("At least one substitution was expected")))
                .map(s -> s.orientate(priorityVariables));
    }

    protected ImmutableList<ImmutableSubstitution<VariableOrGroundTerm>> extractSubstitutions(
            ImmutableCollection<PredicateLevelProposal> predicateProposals) {
        ImmutableList.Builder<ImmutableSubstitution<VariableOrGroundTerm>> substitutionListBuilder = ImmutableList.builder();
        for (PredicateLevelProposal proposal : predicateProposals) {
            substitutionListBuilder.addAll(proposal.getSubstitutions());
        }
        return substitutionListBuilder.build();
    }

    protected <N extends JoinOrFilterNode> NodeCentricOptimizationResults<N> updateJoinNodeAndPropagateSubstitution(
            IntermediateQuery query,
            QueryTreeComponent treeComponent,
            N joinNode,
            ConcreteProposal proposal) throws EmptyQueryException {

        Optional<ImmutableExpression> optionalFilter = joinNode.getOptionalFilterCondition()
                .map(f -> proposal.getOptionalIsNotNullExpression()
                        .map(f2 -> termFactory.getConjunction(f, f2))
                        .orElse(f))
                .map(Optional::of)
                .orElseGet(proposal::getOptionalIsNotNullExpression);

        switch(treeComponent.getChildren(joinNode).size()) {
            case 0:
                throw new IllegalStateException("Self-join elimination MUST not eliminate ALL the nodes");

                /*
                 * Special case: only one child remains so the join node is not needed anymore.
                 *
                 * Creates a FILTER node if there is a joining condition
                 */
            case 1:
                QueryNode uniqueChild = treeComponent.getFirstChild(joinNode).get();

                if (optionalFilter.isPresent()) {
                    QueryNode filterNode = query.getFactory().createFilterNode(optionalFilter.get());
                    treeComponent.replaceNode(joinNode, filterNode);
                }
                else {
                    treeComponent.removeOrReplaceNodeByUniqueChild(joinNode);
                }

                NodeCentricOptimizationResults<QueryNode> propagationResults = propagateSubstitution(query,
                        proposal.getOptionalSubstitution(), uniqueChild);
                /*
                 * Converts it into NodeCentricOptimizationResults over the focus node
                 */
                return propagationResults.getNewNodeOrReplacingChild()
                        // Replaces a descendant
                        .map(child -> new NodeCentricOptimizationResultsImpl<N>(query, Optional.of(child)))
                        // The sub-tree of the join is removed
                        .orElseGet(() -> new NodeCentricOptimizationResultsImpl<N>(query,
                                propagationResults.getOptionalNextSibling(),
                                propagationResults.getOptionalClosestAncestor()));
            /*
             * Multiple children, keep a join node BUT MOVES ITS CONDITION ABOVE (in a filter)
             */
            default:
                N newJoinNode = optionalFilter
                        .map(cond -> {
                            FilterNode parentFilter = query.getFactory().createFilterNode(cond);
                            treeComponent.insertParent(joinNode, parentFilter);

                            N conditionLessJoinNode = (N) ((JoinLikeNode) joinNode)
                                    .changeOptionalFilterCondition(Optional.empty());
                            treeComponent.replaceNode(joinNode, conditionLessJoinNode);
                            return conditionLessJoinNode;
                        })
                        .orElse(joinNode);
                return propagateSubstitution(query, proposal.getOptionalSubstitution(), newJoinNode);
        }
    }

    /**
     *  Applies the substitution from the topNode.
     *
     *  NB: the topNode can be a JoinLikeNode, a replacing FilterNode or the replacing child
     */
    private static <T extends QueryNode> NodeCentricOptimizationResults<T> propagateSubstitution(
            IntermediateQuery query,
            Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalSubstitution,
            T topNode) throws EmptyQueryException {
        if (optionalSubstitution.isPresent()) {

            // TODO: filter the non-bound variables from the substitution before propagating!!!

            SubstitutionPropagationProposal<T> propagationProposal = new SubstitutionPropagationProposalImpl<>(
                    topNode, optionalSubstitution.get(), false);

            // Forces the use of an internal executor (the treeComponent must remain the same).
            return query.applyProposal(propagationProposal, true);
        }
        /*
         * No substitution to propagate
         */
        else {
            return new NodeCentricOptimizationResultsImpl<>(query, topNode);
        }

    }

    /**
     * TODO: implement seriously
     */
    protected ImmutableList<Variable> prioritizeVariables(IntermediateQuery query, JoinLikeNode joinLikeNode) {

        /*
         * Sequential (order matters)
         */
        return extractAncestors(query, joinLikeNode).stream()
                .sequential()
                .flatMap(p -> extractPriorityVariables(query, p.parent, p.position))
                .distinct()
                .collect(ImmutableCollectors.toList());
    }

    private ImmutableList<ParentAndChildPosition> extractAncestors(IntermediateQuery query, QueryNode node) {
        // Non-final
        Optional<QueryNode> optionalAncestor = query.getParent(node);
        QueryNode ancestorChild = node;

        ImmutableList.Builder<ParentAndChildPosition> listBuilder = ImmutableList.builder();

        while (optionalAncestor.isPresent()) {
            QueryNode ancestor = optionalAncestor.get();
            listBuilder.add(new ParentAndChildPosition(ancestor, query.getOptionalPosition(ancestor, ancestorChild)));

            optionalAncestor = query.getParent(ancestor);
            ancestorChild = ancestor;
        }

        return listBuilder.build();
    }


    private Stream<Variable> extractPriorityVariables(IntermediateQuery query, QueryNode node,
                                                      Optional<ArgumentPosition> childPosition) {
        if (node instanceof ExplicitVariableProjectionNode)
            return ((ExplicitVariableProjectionNode)node).getVariables().stream();

        /*
         * LJ: look for variables on the left
         *     only when the focus node is on the right
         */
        else if (node instanceof LeftJoinNode) {
            switch (childPosition.get()) {
                case RIGHT:
                    return query.getChild(node, LEFT)
                            .map(c -> query.getVariables(c).stream())
                            .orElseThrow(() -> new IllegalStateException("A LJ must have a left child"));
                case LEFT:
                default:
                    return Stream.empty();
            }
        }
        else {
            return Stream.empty();
        }
    }

}
