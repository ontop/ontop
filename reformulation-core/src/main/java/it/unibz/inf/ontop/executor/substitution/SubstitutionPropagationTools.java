package it.unibz.inf.ontop.executor.substitution;


import java.util.Optional;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.model.VariableOrGroundTerm;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition;
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
                                                               final ImmutableSubstitution<? extends ImmutableTerm> currentSubstitutionToPropagate,
                                                               final QueryTreeComponent treeComponent)
            throws QueryNodeSubstitutionException {

        Queue<QueryNode> nodesToVisit = new LinkedList<>();
        nodesToVisit.addAll(treeComponent.getCurrentSubNodesOf(focusNode));

        while (!nodesToVisit.isEmpty()) {
            QueryNode formerSubNode = nodesToVisit.poll();

            SubstitutionResults<? extends QueryNode> substitutionResults =
                    formerSubNode.applyDescendingSubstitution(currentSubstitutionToPropagate);

            Optional<? extends QueryNode> optionalNewSubNode = substitutionResults.getOptionalNewNode();
            Optional<? extends ImmutableSubstitution<? extends ImmutableTerm>> optionalNewSubstitution
                    = substitutionResults.getSubstitutionToPropagate();

            /**
             * Still a substitution to propagate
             */
            if (optionalNewSubstitution.isPresent()) {
                ImmutableSubstitution<? extends ImmutableTerm> newSubstitution = optionalNewSubstitution.get();

                /**
                 * No substitution change
                 */
                if (newSubstitution.equals(currentSubstitutionToPropagate)) {
                    /**
                     * Normal case: applies the same substitution to the children
                     */
                    if (optionalNewSubNode.isPresent()) {
                        QueryNode newSubNode = optionalNewSubNode.get();

                        nodesToVisit.addAll(treeComponent.getCurrentSubNodesOf(formerSubNode));
                        treeComponent.replaceNode(formerSubNode, newSubNode);
                    }
                    /**
                     * The sub-node is not needed anymore
                     */
                    else {
                        nodesToVisit.addAll(treeComponent.getCurrentSubNodesOf(formerSubNode));
                        treeComponent.removeOrReplaceNodeByUniqueChildren(formerSubNode);
                    }
                }
                /**
                 * New substitution: applies it to the children
                 */
                else if (optionalNewSubNode.isPresent())  {

                    QueryNode newSubNode = optionalNewSubNode.get();
                    treeComponent.replaceNode(formerSubNode, newSubNode);

                    // Recursive call
                    propagateSubstitutionDown(newSubNode, newSubstitution, treeComponent);
                }
                /**
                 * Unhandled case: new substitution to apply to the children of
                 * a not-needed node.
                 *
                 * TODO: should we handle this case
                 */
                else {
                    throw new RuntimeException("Unhandled case: new substitution to apply to the children of " +
                            "a not-needed node.");
                }
            }
            /**
             * Stops the propagation
             */
            else {
                if (optionalNewSubNode.isPresent()) {
                    QueryNode newSubNode = optionalNewSubNode.get();
                    treeComponent.replaceNode(formerSubNode, newSubNode);
                }
                else {
                    throw new RuntimeException("Unhandled case: the stopping node for the propagation" +
                            "is not needed anymore. Should we support it?");
                }
            }
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
            T focusNode, ImmutableSubstitution<? extends VariableOrGroundTerm> substitutionToPropagate,
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

            Optional<? extends ImmutableSubstitution<? extends ImmutableTerm>> optionalNewSubstitution =
                    substitutionResults.getSubstitutionToPropagate();
            Optional<? extends QueryNode> optionalNewAncestor = substitutionResults.getOptionalNewNode();

            /**
             * Ancestor is empty --> applies a ReactToChildDeletionProposal and returns the remaining ancestor
             */
            if (substitutionResults.isNodeEmpty()) {
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
                return new NodeCentricOptimizationResultsImpl<T>(query, Optional.empty(),
                        Optional.of(reactionResults.getClosestRemainingAncestor()));
            }
            else {

                /**
                 * Normal case: replace the ancestor by an updated version
                 */
                if (optionalNewAncestor.isPresent()) {

                    QueryNode newAncestor = optionalNewAncestor.get();
                    treeComponent.replaceNode(currentAncestor, newAncestor);
                }
                /**
                 * The ancestor is not needed anymore
                 */
                else {
                    /**
                     * If a position is specified, removes the other children
                     */
                    substitutionResults.getOptionalReplacingChildPosition()
                            .ifPresent(position -> query.getChildren(currentAncestor).stream()
                                    // Not the same position
                                    .filter(c -> query.getOptionalPosition(currentAncestor, c)
                                            .filter(p -> p.equals(position))
                                            .isPresent())
                                    .forEach(treeComponent::removeSubTree));

                    // Assume there is only one child
                    treeComponent.removeOrReplaceNodeByUniqueChildren(currentAncestor);
                }

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
        }

        /**
         * If no ancestor has been
         */
        return new NodeCentricOptimizationResultsImpl<>(query, focusNode);
    }
}
