package it.unibz.inf.ontop.executor.substitution;


import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.pivotalrepr.proposal.AncestryTracker;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.EmptyNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.RemoveEmptyNodeProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.AncestryTrackingResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.AncestryTrackingResultsImpl;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.RemoveEmptyNodeProposalImpl;

import java.util.stream.Stream;

import static it.unibz.inf.ontop.executor.substitution.DescendingPropagationTools.propagateSubstitutionDownToNodes;

/**
 * These methods are only accessible by InternalProposalExecutors (requires access to the QueryTreeComponent).
 */
public class AscendingPropagationTools {


    /**
     * TODO: find a better term
     */
    private static class DescendingPropagationParams {

        public final QueryNode focusNode;
        public final Stream<QueryNode> otherChildren;
        public final ImmutableSubstitution<? extends ImmutableTerm> substitution;

        public DescendingPropagationParams(QueryNode focusNode, Stream<QueryNode> otherChildren,
                                           ImmutableSubstitution<? extends ImmutableTerm> substitution) {
            this.focusNode = focusNode;
            this.otherChildren = otherChildren;
            this.substitution = substitution;
        }
    }

    /**
     * Propagates the substitution to the ancestors of the focus newNode.
     *
     * THE SUBSTITUTION IS NOT APPLIED TO THE FOCUS NODE nor to its siblings.
     *
     * Has-side effect on the tree component.
     *
     * Note that some ancestors may become empty and thus the focus newNode and its siblings be removed.
     *
     * TODO: clean
     *
     */
    public static <T extends QueryNode> AncestryTrackingResults<T> propagateSubstitutionUp(
            T focusNode, ImmutableSubstitution<? extends ImmutableTerm> substitutionToPropagate,
            IntermediateQuery query, QueryTreeComponent treeComponent,
            Optional<AncestryTracker> optionalAncestryTracker) throws QueryNodeSubstitutionException,
            EmptyQueryException {

        // Propagations to be apply into branches after the ascendant one
        ImmutableList.Builder<DescendingPropagationParams> descendingPropagParamBuilder = ImmutableList.builder();

        // Non-final
        Optional<QueryNode> optionalCurrentAncestor = query.getParent(focusNode);
        QueryNode ancestorChild = focusNode;

        // Non-final
        ImmutableSubstitution<? extends ImmutableTerm> currentSubstitution = substitutionToPropagate;

        while (optionalCurrentAncestor.isPresent()) {
            final QueryNode currentAncestor = optionalCurrentAncestor.get();
            QueryNode futureChild = currentAncestor;

            final Optional<QueryNode> optionalNextAncestor = query.getParent(currentAncestor);

            /**
             * Applies the substitution and analyses the proposed results
             */
            SubstitutionResults<? extends QueryNode> substitutionResults = currentAncestor.applyAscendingSubstitution(
                    currentSubstitution, ancestorChild, query);


            Stream<QueryNode> otherChildren;

            switch (substitutionResults.getLocalAction()) {
                case NO_CHANGE:
                    otherChildren = query.getOtherChildrenStream(currentAncestor, ancestorChild);
                    break;
                case NEW_NODE:
                    QueryNode newAncestor = substitutionResults.getOptionalNewNode().get();
                    if (currentAncestor != newAncestor) {
                        treeComponent.replaceNode(currentAncestor, newAncestor);
                        optionalAncestryTracker.ifPresent(tr -> tr.recordReplacement(currentAncestor, newAncestor));
                    }
                    otherChildren = query.getOtherChildrenStream(newAncestor, ancestorChild);
                    futureChild = newAncestor;
                    break;
                case INSERT_CONSTRUCTION_NODE:
                    QueryNode downgradedChildNode = substitutionResults.getOptionalDowngradedChildNode().get();
                    otherChildren = query.getOtherChildrenStream(currentAncestor, downgradedChildNode);

                    Optional<? extends QueryNode> optionalUpdatedAncestor = substitutionResults.getOptionalNewNode();
                    if (optionalUpdatedAncestor.isPresent()) {
                        QueryNode updatedAncestor = optionalUpdatedAncestor.get();
                        treeComponent.replaceNode(currentAncestor, updatedAncestor);
                        futureChild = updatedAncestor;
                    }

                    ConstructionNode newParentOfChildNode = substitutionResults
                            .getOptionalNewParentOfChildNode().get();
                    treeComponent.insertParent(downgradedChildNode, newParentOfChildNode);
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
                    QueryNode replacingChild = treeComponent.removeOrReplaceNodeByUniqueChildren(currentAncestor);

                    optionalAncestryTracker.ifPresent(tr -> tr.recordReplacementByChild(currentAncestor, replacingChild));

                    otherChildren = ancestorChild != replacingChild
                            ? Stream.of(replacingChild)
                            : Stream.of();
                    futureChild = replacingChild;
                    break;
                /**
                 * Ancestor is empty --> removes it and returns the closest ancestor + the next sibling
                 */
                case DECLARE_AS_EMPTY:
                    AncestryTrackingResults<EmptyNode> removalResults =
                            reactToEmptinessDeclaration(query, currentAncestor, treeComponent);

                    return new AncestryTrackingResultsImpl<>(query, removalResults.getOptionalNextSibling(),
                            removalResults.getOptionalClosestAncestor(), removalResults.getOptionalTracker());
                default:
                    throw new IllegalStateException("Unknown local action: " + substitutionResults.getLocalAction());
            }

            Optional<? extends ImmutableSubstitution<? extends ImmutableTerm>> optionalNewSubstitution =
                    substitutionResults.getSubstitutionToPropagate();

            /**
             * Plans to propagate the substitution DOWN to the other children (will be done later)
             */
            if (optionalNewSubstitution.isPresent()) {
                descendingPropagParamBuilder.add(new DescendingPropagationParams(futureChild, otherChildren,
                        optionalNewSubstitution.get()));
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

            ancestorChild = futureChild;
        }

        return applyDescendingPropagations(descendingPropagParamBuilder.build(), query, treeComponent,
                focusNode, optionalAncestryTracker);
    }

    /**
     * TODO: explain
     *
     * Applies descending propagations in some other branches
     *
     */
    private static <T extends QueryNode> AncestryTrackingResults<T> applyDescendingPropagations(
            ImmutableList<DescendingPropagationParams> propagations, IntermediateQuery query,
            QueryTreeComponent treeComponent, T originalFocusNode, Optional<AncestryTracker> optionalAncestryTracker) throws EmptyQueryException {

        for(DescendingPropagationParams params : propagations) {
            if (!query.contains(params.focusNode)) {
                break;
            }

            NodeCentricOptimizationResults<QueryNode> propagationResults = propagateSubstitutionDownToNodes(
                        params.focusNode, params.otherChildren, params.substitution, query, treeComponent);

            optionalAncestryTracker.ifPresent(tr -> tr.recordResults(propagationResults));
        }

        if (query.contains(originalFocusNode)) {
            return new AncestryTrackingResultsImpl<>(query, originalFocusNode, optionalAncestryTracker);
        }
        else {
            throw new RuntimeException("TODO: support the case where the focus node is removed");
        }
    }

    /**
     * Returns results centered on the removed node.
     */
    private static AncestryTrackingResults<EmptyNode> reactToEmptinessDeclaration(
            IntermediateQuery query, QueryNode currentAncestor, QueryTreeComponent treeComponent) throws EmptyQueryException {

        ImmutableSet<Variable> nullVariables = query.getProjectedVariables(currentAncestor);
        EmptyNode replacingEmptyNode = new EmptyNodeImpl(nullVariables);

        treeComponent.replaceSubTree(currentAncestor, replacingEmptyNode);

        RemoveEmptyNodeProposal proposal = new RemoveEmptyNodeProposalImpl(replacingEmptyNode, false);
        return query.applyProposal(proposal, true);
    }
}
