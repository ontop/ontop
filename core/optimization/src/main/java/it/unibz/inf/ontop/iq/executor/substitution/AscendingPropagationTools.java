package it.unibz.inf.ontop.iq.executor.substitution;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeSubstitutionException;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.EmptyNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.SubstitutionResults;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.QueryTreeComponent;
import it.unibz.inf.ontop.iq.proposal.NodeTracker;
import it.unibz.inf.ontop.iq.proposal.NodeTrackingResults;
import it.unibz.inf.ontop.iq.proposal.RemoveEmptyNodeProposal;
import it.unibz.inf.ontop.iq.proposal.impl.NodeTrackerImpl;
import it.unibz.inf.ontop.iq.proposal.impl.NodeTrackingResultsImpl;
import it.unibz.inf.ontop.iq.proposal.impl.RemoveEmptyNodeProposalImpl;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.executor.substitution.DescendingPropagationTools.propagateSubstitutionDownToNodes;

/**
 * These methods are only accessible by InternalProposalExecutors (requires access to the QueryTreeComponent).
 */
public class AscendingPropagationTools {


    /**
     * Wrapper of what can be returned when applying the ascending substitution to an ancestor
     */
    private static class AncestorPropagationResults<N extends QueryNode> {

        public final Optional<NodeTrackingResults<N>> optionalAncestryTrackingResults;
        public final Optional<ImmutableSubstitution<? extends ImmutableTerm>> optionalSubstitutionToPropagate;
        public final Optional<QueryNode> optionalNextAncestor;
        public final Optional<QueryNode> optionalChildOfNextAncestor;
        public final Optional<DescendingPropagationParams> optionalDescendingPropagParams;

        /**
         * Case 1: empty ancestor --> returns the results of its removal
         */
        public AncestorPropagationResults(NodeTrackingResults<N> nodeTrackingResults) {
            this.optionalAncestryTrackingResults = Optional.of(nodeTrackingResults);
            this.optionalSubstitutionToPropagate = Optional.empty();
            this.optionalNextAncestor = Optional.empty();
            this.optionalChildOfNextAncestor = Optional.empty();
            this.optionalDescendingPropagParams = Optional.empty();
        }

        /**
         * Case 2: continues the ascending propagation
         */
        public AncestorPropagationResults(ImmutableSubstitution<? extends ImmutableTerm> substitutionToPropagate,
                                          Optional<QueryNode> optionalNextAncestor,
                                          QueryNode childOfNextAncestor,
                                          Optional<DescendingPropagationParams> optionalDescendingPropagParams) {
            this.optionalAncestryTrackingResults = Optional.empty();
            this.optionalSubstitutionToPropagate = Optional.of(substitutionToPropagate);
            this.optionalNextAncestor = optionalNextAncestor;
            this.optionalChildOfNextAncestor = Optional.of(childOfNextAncestor);
            this.optionalDescendingPropagParams = optionalDescendingPropagParams;
        }

        /**
         * Case 3: stops the ascending propagation
         */
        public AncestorPropagationResults(Optional<DescendingPropagationParams> optionalDescendingPropagParams) {
            this.optionalAncestryTrackingResults = Optional.empty();
            this.optionalSubstitutionToPropagate = Optional.empty();
            this.optionalNextAncestor = Optional.empty();
            this.optionalChildOfNextAncestor = Optional.empty();
            this.optionalDescendingPropagParams = optionalDescendingPropagParams;
        }
    }


    /**
     * TODO: find a better term
     */
    private static class DescendingPropagationParams {

        public final QueryNode focusNode;
        public final ImmutableList<QueryNode> otherChildren;
        public final ImmutableSubstitution<? extends ImmutableTerm> substitution;

        public DescendingPropagationParams(QueryNode focusNode, Stream<QueryNode> otherChildren,
                                           ImmutableSubstitution<? extends ImmutableTerm> substitution) {
            this.focusNode = focusNode;
            this.otherChildren = otherChildren.collect(ImmutableCollectors.toList());
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
     */
    public static <N extends QueryNode> NodeTrackingResults<N> propagateSubstitutionUp(
            N focusNode, ImmutableSubstitution<? extends ImmutableTerm> substitutionToPropagate,
            IntermediateQuery query, QueryTreeComponent treeComponent,
            IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory,
            Optional<NodeTracker> optionalAncestryTracker)
            throws QueryNodeSubstitutionException, EmptyQueryException {

        // Substitutions to be propagated down into branches after the ascendant one
        ImmutableList.Builder<DescendingPropagationParams> descendingPropagParamBuilder = ImmutableList.builder();

        // Non-final
        Optional<QueryNode> optionalCurrentAncestor = query.getParent(focusNode);
        QueryNode ancestorChild = focusNode;
        ImmutableSubstitution<? extends ImmutableTerm> currentSubstitution = substitutionToPropagate;

        /*
         * Special case: the focus node is the root
         */
        if (!optionalCurrentAncestor.isPresent()) {
            insertRootConstructionNode(query, treeComponent, substitutionToPropagate, iqFactory, substitutionFactory);
        }

        /*
         * Iterates over the ancestors until nothing needs to be propagated
         * or an ancestor is declared as empty (it is then immediately removed).
         */
        while (optionalCurrentAncestor.isPresent()) {
            AncestorPropagationResults<N> results = applyAscendingSubstitutionToAncestor(query, optionalCurrentAncestor.get(),
                    currentSubstitution, ancestorChild, treeComponent, optionalAncestryTracker);

            // Case 1:  empty ancestor --> returns the results of its removals
            if (results.optionalAncestryTrackingResults.isPresent()) {
                return results.optionalAncestryTrackingResults.get();
            }
            // Case 2: continues the propagation
            else if (results.optionalNextAncestor.isPresent()) {
                ancestorChild = results.optionalChildOfNextAncestor.get();
                currentSubstitution = results.optionalSubstitutionToPropagate.get();
            }
            /*
             * Case 3: stops the propagation and inserts a top construction node if necessary
             */
            else {
                results.optionalSubstitutionToPropagate
                        .filter(s -> !s.isEmpty())
                        .ifPresent(s -> insertRootConstructionNode(query, treeComponent, s, iqFactory, substitutionFactory));
            }

            results.optionalDescendingPropagParams
                    .ifPresent(descendingPropagParamBuilder::add);

            // May stop the propagation (case 3)
            optionalCurrentAncestor = results.optionalNextAncestor;
        }

        return applyDescendingPropagations(descendingPropagParamBuilder.build(), query, treeComponent,
                focusNode, optionalAncestryTracker);
    }

    private static void insertRootConstructionNode(IntermediateQuery query,
                                                   QueryTreeComponent treeComponent,
                                                   ImmutableSubstitution<? extends ImmutableTerm> propagatedSubstitution,
                                                   IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory) {
        ImmutableSet<Variable> projectedVariables = query.getProjectionAtom().getVariables();

        ImmutableMap<Variable, ImmutableTerm> newSubstitutionMap = propagatedSubstitution.getImmutableMap().entrySet().stream()
                .filter(e -> projectedVariables.contains(e.getKey()))
                .collect(ImmutableCollectors.toMap(
                        Map.Entry::getKey,
                        e -> (ImmutableTerm) e.getValue()));

        ConstructionNode newRootNode = iqFactory.createConstructionNode(projectedVariables,
                substitutionFactory.getSubstitution(newSubstitutionMap));

        treeComponent.insertParent(treeComponent.getRootNode(), newRootNode);
    }

    /**
     * Applies the ascending substitution to one ancestor
     */
    private static <N extends QueryNode> AncestorPropagationResults<N> applyAscendingSubstitutionToAncestor(
            IntermediateQuery query, QueryNode currentAncestor,
            ImmutableSubstitution<? extends ImmutableTerm> currentSubstitution,
            QueryNode childOfAncestor,
            QueryTreeComponent treeComponent, Optional<NodeTracker> optionalAncestryTracker) throws EmptyQueryException {

        // Non-final
        QueryNode childOfNextAncestor = currentAncestor;

        final Optional<QueryNode> optionalNextAncestor = query.getParent(currentAncestor);

        /**
         * Applies the substitution and analyses the proposed results.
         *
         * Keeps track of the changes by updating the ancestry tracker.
         *
         */
        final SubstitutionResults<? extends QueryNode> substitutionResults = currentAncestor.applyAscendingSubstitution(
                currentSubstitution, childOfAncestor, query);

        final Stream<QueryNode> otherChildren;

        switch (substitutionResults.getLocalAction()) {
            case NO_CHANGE:
                otherChildren = query.getOtherChildrenStream(currentAncestor, childOfAncestor);
                break;
            case NEW_NODE:
                QueryNode newAncestor = substitutionResults.getOptionalNewNode().get();
                if (currentAncestor != newAncestor) {
                    treeComponent.replaceNode(currentAncestor, newAncestor);
                    optionalAncestryTracker.ifPresent(tr -> tr.recordReplacement(currentAncestor, newAncestor));
                }
                otherChildren = query.getOtherChildrenStream(newAncestor, childOfAncestor);
                childOfNextAncestor = newAncestor;
                break;
            case INSERT_CONSTRUCTION_NODE:
                QueryNode downgradedChildNode = substitutionResults.getOptionalDowngradedChildNode().get();
                otherChildren = query.getOtherChildrenStream(currentAncestor, downgradedChildNode);

                Optional<? extends QueryNode> optionalUpdatedAncestor = substitutionResults.getOptionalNewNode();
                if (optionalUpdatedAncestor.isPresent()) {
                    QueryNode updatedAncestor = optionalUpdatedAncestor.get();
                    treeComponent.replaceNode(currentAncestor, updatedAncestor);
                    childOfNextAncestor = updatedAncestor;
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
                QueryNode replacingChild = query.getFirstChild(currentAncestor)
                        .orElseThrow(() -> new IllegalStateException(currentAncestor + " has no child " +
                                "so cannot be replaced by it!"));

                optionalAncestryTracker.ifPresent(tr -> tr.recordUpcomingReplacementByChild(query, currentAncestor,
                        replacingChild));

                treeComponent.removeOrReplaceNodeByUniqueChild(currentAncestor);

                otherChildren = childOfAncestor != replacingChild
                        ? Stream.of(replacingChild)
                        : Stream.of();
                childOfNextAncestor = replacingChild;
                break;
            /**
             * Ancestor is empty --> removes it and returns the closest ancestor + the next sibling
             */
            case DECLARE_AS_EMPTY:
                NodeTrackingResults<EmptyNode> removalResults =
                        reactToEmptinessDeclaration(query, currentAncestor, treeComponent, optionalAncestryTracker);

                return new AncestorPropagationResults<>(
                        new NodeTrackingResultsImpl<>(query, removalResults.getOptionalNextSibling(),
                                removalResults.getOptionalClosestAncestor(), removalResults.getOptionalTracker()));
            default:
                throw new IllegalStateException("Unknown local action: " + substitutionResults.getLocalAction());
        }

        Optional<? extends ImmutableSubstitution<? extends ImmutableTerm>> optionalNewSubstitution =
                substitutionResults.getSubstitutionToPropagate();

        /**
         * Plans to propagate the substitution DOWN to the other children (will be done later)
         */
        Optional<DescendingPropagationParams> optionalDescendingPropagParams = optionalNewSubstitution.isPresent()
                ? Optional.of(new DescendingPropagationParams(childOfNextAncestor, otherChildren, optionalNewSubstitution.get()))
                : Optional.empty();


        /**
         * Continues the propagation
         */
        if (optionalNewSubstitution.isPresent()) {
            return new AncestorPropagationResults<>(optionalNewSubstitution.get(), optionalNextAncestor, childOfNextAncestor,
                    optionalDescendingPropagParams);
        }
        /**
         * Or stops it
         */
        else {
            return new AncestorPropagationResults<>(optionalDescendingPropagParams);
        }
    }


    /**
     * TODO: explain
     *
     * Propagates descending substitutions in some other branches
     *
     */
    private static <T extends QueryNode> NodeTrackingResults<T> applyDescendingPropagations(
            ImmutableList<DescendingPropagationParams> propagations, IntermediateQuery query,
            QueryTreeComponent treeComponent, T originalFocusNode,
            Optional<NodeTracker> optionalNodeTracker) throws EmptyQueryException {

        NodeTracker tracker = optionalNodeTracker
                .orElseGet(() -> new NodeTrackerImpl());

        for(DescendingPropagationParams params : propagations) {
            if (!query.contains(params.focusNode)) {
                break;
            }

            propagateSubstitutionDownToNodes(params.focusNode, params.otherChildren.stream(), params.substitution,
                    query, treeComponent, Optional.of(tracker));
        }

        NodeTracker.NodeUpdate<T> update = tracker.getUpdate(query, originalFocusNode);

        return update.getNewNode()
                .map(n -> new NodeTrackingResultsImpl<>(query, n, optionalNodeTracker))
                .orElseGet(() -> update.getReplacingChild()
                        .map(n -> new NodeTrackingResultsImpl<T>(query, Optional.of(n), optionalNodeTracker))
                        .orElseGet(() -> new NodeTrackingResultsImpl<T>(query, update.getOptionalNextSibling(query),
                                update.getOptionalClosestAncestor(query), optionalNodeTracker)));
    }

    /**
     * Returns results centered on the removed node.
     */
    private static NodeTrackingResults<EmptyNode> reactToEmptinessDeclaration(
            IntermediateQuery query, QueryNode currentAncestor, QueryTreeComponent treeComponent,
            Optional<NodeTracker> optionalAncestryTracker) throws EmptyQueryException {

        ImmutableSet<Variable> nullVariables = query.getVariables(currentAncestor);
        EmptyNode replacingEmptyNode = query.getFactory().createEmptyNode(nullVariables);

        treeComponent.replaceSubTree(currentAncestor, replacingEmptyNode);

        RemoveEmptyNodeProposal proposal =
                optionalAncestryTracker.isPresent()
                ? new RemoveEmptyNodeProposalImpl(replacingEmptyNode, optionalAncestryTracker.get())
                : new RemoveEmptyNodeProposalImpl(replacingEmptyNode, false);
        return query.applyProposal(proposal, true);
    }
}
