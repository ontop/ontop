package it.unibz.inf.ontop.executor.substitution;


import java.util.Optional;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition;
import it.unibz.inf.ontop.pivotalrepr.impl.EmptyNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.ReactToChildDeletionProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.ReactToChildDeletionResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.ReactToChildDeletionProposalImpl;
import it.unibz.inf.ontop.pivotalrepr.unfolding.ProjectedVariableExtractionTools;

import java.util.LinkedList;
import java.util.Queue;

import static it.unibz.inf.ontop.pivotalrepr.unfolding.ProjectedVariableExtractionTools.extractProjectedVariables;

/**
 * These methods are only accessible by InternalProposalExecutors (requires access to the QueryTreeComponent).
 */
public class SubstitutionPropagationTools {

    private static class NodeAndSubstitution {
        public final QueryNode node;
        /**
         * Substitution to propagate to this node
         */
        public final ImmutableSubstitution<? extends ImmutableTerm> substitution;

        private NodeAndSubstitution(QueryNode node,
                                    ImmutableSubstitution<? extends ImmutableTerm> substitution) {
            this.node = node;
            this.substitution = substitution;
        }
    }

    /**
     * Propagates the substitution to the descendants of the focus node.
     *
     * THE SUBSTITUTION IS NOT APPLIED TO THE FOCUS NODE.
     *
     * Has-side effect on the tree component.
     * Returns the updated tree component
     *
     */
    public static QueryTreeComponent propagateSubstitutionDown(final QueryNode focusNode,
                                                               final ImmutableSubstitution<? extends ImmutableTerm> initialSubstitutionToPropagate,
                                                               final IntermediateQuery query,
                                                               final QueryTreeComponent treeComponent)
            throws QueryNodeSubstitutionException {

        Queue<NodeAndSubstitution> nodeAndSubsToVisit = new LinkedList<>();
        treeComponent.getCurrentSubNodesOf(focusNode).stream()
                .map(n -> new NodeAndSubstitution(n, initialSubstitutionToPropagate))
                .forEach(nodeAndSubsToVisit::add);

        while (!nodeAndSubsToVisit.isEmpty()) {

            NodeAndSubstitution currentNodeAndSubstitution = nodeAndSubsToVisit.poll();
            SubstitutionResults<? extends QueryNode> substitutionResults =
                    currentNodeAndSubstitution.node.applyDescendingSubstitution(
                            currentNodeAndSubstitution.substitution, query);

            /**
             * Applies the local action
             */
            final QueryNode currentNode;
            switch (substitutionResults.getLocalAction()) {
                case NEW_NODE:
                    QueryNode newNode = substitutionResults.getOptionalNewNode()
                            .orElseThrow(() -> new IllegalStateException("A new node was expected"));
                    treeComponent.replaceNode(currentNodeAndSubstitution.node, newNode);
                    currentNode = newNode;
                    break;
                case NO_CHANGE:
                    currentNode = currentNodeAndSubstitution.node;
                    break;
                case REPLACE_BY_CHILD:
                    QueryNode replacingChild = substitutionResults.getOptionalReplacingChildPosition()
                            .flatMap(position -> query.getChild(currentNodeAndSubstitution.node, position))
                            .orElseGet(() -> query.getFirstChild(currentNodeAndSubstitution.node)
                                    .orElseThrow(() -> new IllegalStateException("No replacing child is available")));

                    treeComponent.replaceNodeByChild(currentNodeAndSubstitution.node,
                            substitutionResults.getOptionalReplacingChildPosition());

                    currentNode = replacingChild;
                    break;
                case INSERT_CONSTRUCTION_NODE:
                    throw new IllegalStateException("Construction node insertion not expected " +
                            "while pushing a substitution down");
                    /**
                     * Replace the sub-tree by an empty node
                     */
                case DECLARE_AS_EMPTY:
                    QueryNode replacingNode = new EmptyNodeImpl(
                            extractProjectedVariables(query, currentNodeAndSubstitution.node));
                    treeComponent.replaceSubTree(currentNodeAndSubstitution.node, replacingNode);
                    currentNode = replacingNode;
                    break;

                default:
                    throw new IllegalStateException("Unknown local action: " + substitutionResults.getLocalAction());
            }

            /**
             * Adds the children - new substitution pairs to the queue
             */
            substitutionResults.getSubstitutionToPropagate()
                    .ifPresent(newSubstitution -> treeComponent.getCurrentSubNodesOf(currentNode).stream()
                            .map(n -> new NodeAndSubstitution(n, newSubstitution))
                            .forEach(nodeAndSubsToVisit::add));
        }
        return treeComponent;
    }

    /**
     * Propagates the substitution to the ancestors of the focus node.
     *
     * THE SUBSTITUTION IS NOT APPLIED TO THE FOCUS NODE nor to its siblings.
     *
     * Has-side effect on the tree component.
     *
     * Note that some ancestors may become empty and thus the focus node and its siblings be removed.
     *
     * TODO: clean
     *
     */
    public static <T extends QueryNode> NodeCentricOptimizationResults<T> propagateSubstitutionUp(
            T focusNode, ImmutableSubstitution<? extends ImmutableTerm> substitutionToPropagate,
            IntermediateQuery query, QueryTreeComponent treeComponent) throws QueryNodeSubstitutionException,
            EmptyQueryException {

        // Non-final
        Optional<QueryNode> optionalCurrentAncestor = query.getParent(focusNode);

        // Non-final
        ImmutableSubstitution<? extends ImmutableTerm> currentSubstitution = substitutionToPropagate;


        while (optionalCurrentAncestor.isPresent()) {
            final QueryNode currentAncestor = optionalCurrentAncestor.get();

            final Optional<QueryNode> optionalNextAncestor = query.getParent(currentAncestor);

            /**
             * Applies the substitution and analyses the results
             */
            SubstitutionResults<? extends QueryNode> substitutionResults = currentAncestor.applyAscendingSubstitution(
                    currentSubstitution, focusNode, query);

            switch (substitutionResults.getLocalAction()) {
                case NO_CHANGE:
                    break;
                case NEW_NODE:
                    QueryNode newAncestor = substitutionResults.getOptionalNewNode().get();
                    if (currentAncestor != newAncestor) {
                        treeComponent.replaceNode(currentAncestor, newAncestor);
                    }
                    break;
                case INSERT_CONSTRUCTION_NODE:
                    substitutionResults.getOptionalNewNode()
                            .ifPresent(updatedAncestor -> {
                                if (currentAncestor != updatedAncestor) {
                                    treeComponent.replaceNode(currentAncestor, updatedAncestor);
                                }});

                    ConstructionNode newParentOfDescendantNode = substitutionResults
                            .getOptionalNewParentOfDescendantNode().get();
                    QueryNode descendantNode = substitutionResults.getOptionalDescendantNode().get();
                    treeComponent.insertParent(descendantNode, newParentOfDescendantNode);
                    break;

                case REPLACE_BY_CHILD:
                    /**
                     * If a position is specified, removes the other children
                     */
                    substitutionResults.getOptionalReplacingChildPosition()
                            .ifPresent(position -> query.getChildren(currentAncestor).stream()
                                    // Not the same position
                                    .filter(c -> query.getOptionalPosition(currentAncestor, c)
                                            .filter(p -> ! p.equals(position))
                                            .isPresent())
                                    .forEach(treeComponent::removeSubTree));

                    // Assume there is only one child
                    treeComponent.removeOrReplaceNodeByUniqueChildren(currentAncestor);
                    break;
                /**
                 * Ancestor is empty --> applies a ReactToChildDeletionProposal and returns the remaining ancestor
                 */
                case DECLARE_AS_EMPTY:
                    return reactToEmptinessDeclaration(optionalNextAncestor, query, currentAncestor, treeComponent);
                default:
                    throw new IllegalStateException("Unknown local action: " + substitutionResults.getLocalAction());
            }

            Optional<? extends ImmutableSubstitution<? extends ImmutableTerm>> optionalNewSubstitution =
                    substitutionResults.getSubstitutionToPropagate();

            /**
             * Continue the propagation
             */
            if (optionalNewSubstitution.isPresent()) {

                // Continue with these values
                currentSubstitution = optionalNewSubstitution.get();
                optionalCurrentAncestor = optionalNextAncestor;
            }
            /**
             * Or stop it
             */
            else {
                // Stops
                optionalCurrentAncestor = Optional.empty();
            }
        }

        /**
         * If no ancestor has been
         */
        return new NodeCentricOptimizationResultsImpl<>(query, focusNode);
    }

    private static <T extends QueryNode> NodeCentricOptimizationResults<T> reactToEmptinessDeclaration(
            Optional<QueryNode> optionalNextAncestor, IntermediateQuery query, QueryNode currentAncestor,
            QueryTreeComponent treeComponent) throws EmptyQueryException {
        QueryNode ancestorParent = optionalNextAncestor
                .orElseThrow(EmptyQueryException::new);

        ImmutableSet<Variable> nullVariables = extractProjectedVariables(query, currentAncestor);
        Optional<QueryNode> optionalNextSibling = query.getNextSibling(currentAncestor);
        Optional<ArgumentPosition> optionalPosition = query.getOptionalPosition(ancestorParent, currentAncestor);

        treeComponent.removeSubTree(currentAncestor);

        ReactToChildDeletionProposal reactionProposal = new ReactToChildDeletionProposalImpl(
                ancestorParent, optionalNextSibling, optionalPosition, nullVariables);

        // In-place optimization (returns the same query)
        ReactToChildDeletionResults reactionResults = query.applyProposal(reactionProposal, true);

        // Only returns the closest remaining ancestor
        return new NodeCentricOptimizationResultsImpl<>(query, Optional.empty(),
                Optional.of(reactionResults.getClosestRemainingAncestor()));
    }
}
