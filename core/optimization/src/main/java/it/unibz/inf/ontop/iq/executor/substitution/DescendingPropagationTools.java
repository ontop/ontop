package it.unibz.inf.ontop.iq.executor.substitution;


import it.unibz.inf.ontop.iq.executor.substitution.LocalPropagationTools.SubstitutionApplicationResults;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.exception.QueryNodeSubstitutionException;
import it.unibz.inf.ontop.iq.impl.QueryTreeComponent;
import it.unibz.inf.ontop.iq.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.iq.proposal.NodeTracker;
import it.unibz.inf.ontop.iq.proposal.NodeTrackingResults;
import it.unibz.inf.ontop.iq.proposal.impl.NodeTrackingResultsImpl;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.executor.substitution.LocalPropagationTools.applySubstitutionToNode;

/**
 * TODO: explain
 */
public class DescendingPropagationTools {

    private static class NodeAndSubstitution {
        public final QueryNode node;
        /**
         * Substitution to propagate to this newNode
         */
        public final ImmutableSubstitution<? extends ImmutableTerm> substitution;

        private NodeAndSubstitution(QueryNode node,
                                    ImmutableSubstitution<? extends ImmutableTerm> substitution) {
            this.node = node;
            this.substitution = substitution;
        }
    }

    /**
     * Propagates the substitution to the descendants of the focus newNode.
     *
     * THE SUBSTITUTION IS NOT APPLIED TO THE FOCUS NODE.
     *
     * Has-side effect on the tree component.
     * Returns the updated tree component
     *
     */
    public static <N extends QueryNode> NodeCentricOptimizationResults<N> propagateSubstitutionDown(
            final N focusNode, final ImmutableSubstitution<? extends ImmutableTerm> initialSubstitutionToPropagate,
            final IntermediateQuery query,
            final QueryTreeComponent treeComponent)
            throws QueryNodeSubstitutionException, EmptyQueryException {

        return propagateSubstitutionDownToNodes(focusNode, treeComponent.getChildrenStream(focusNode),
                initialSubstitutionToPropagate, query, treeComponent, Optional.empty());
    }

    /**
     * Applies the substitution to the starting nodes and to their children
     */
    protected static <N extends QueryNode> NodeTrackingResults<N> propagateSubstitutionDownToNodes(
            N originalFocusNode, final Stream<QueryNode> startingNodes,
            final ImmutableSubstitution<? extends ImmutableTerm> initialSubstitutionToPropagate,
            final IntermediateQuery query, final QueryTreeComponent treeComponent,
            Optional<NodeTracker> optionalAncestryTracker)
            throws QueryNodeSubstitutionException, EmptyQueryException {

        Queue<NodeAndSubstitution> nodeAndSubsToVisit = new LinkedList<>();
        startingNodes
                .map(n -> new NodeAndSubstitution(n, initialSubstitutionToPropagate))
                .forEach(nodeAndSubsToVisit::add);

        // Non-final
        Optional<N> optionalFocusNode = Optional.of(originalFocusNode);
        /**
         * In case the focus node is removed (no value before)
         *
         */
        Optional<QueryNode> optionalClosestAncestorOfFocusNode = Optional.empty();
        Optional<QueryNode> optionalNextSiblingOfFocusNode = Optional.empty();


        while (!nodeAndSubsToVisit.isEmpty()) {
            NodeAndSubstitution initialNodeAndSubstitution = nodeAndSubsToVisit.poll();

            /**
             * Some nodes may have be removed in the reaction to the removal of an empty sibling.
             *
             * It is ASSUMED that the removal of a child:
             *    - either leads to the removal of some other siblings
             *    - or have no impact on its siblings beside moving their position (e.g. making them replace their parent).
             *
             * If this assumption holds, it is then safe to ignore a removed/replaced node.
             *
             */
            if (!treeComponent.contains(initialNodeAndSubstitution.node)) {
                break;
            }

            SubstitutionApplicationResults<QueryNode> applicationResults = applySubstitutionToNode(
                    initialNodeAndSubstitution.node, initialNodeAndSubstitution.substitution,
                    query, treeComponent, optionalAncestryTracker);

            /**
             * Adds the children - new substitution pairs to the queue
             */
            applicationResults.getOptionalSubstitution()
                    .ifPresent(newSubstitution -> {
                        if (applicationResults.getNewNodeOrReplacingChild().isPresent()) {
                            QueryNode newNode = applicationResults.getNewNodeOrReplacingChild().get();

                            /**
                             * Special case: replace a child
                             */
                            if (applicationResults.isReplacedByAChild()) {
                                nodeAndSubsToVisit.add(new NodeAndSubstitution(newNode, newSubstitution));
                            }
                            /**
                             * Otherwise, forwards the descending substitution to its children
                             */
                            else {
                                treeComponent.getChildren(newNode).stream()
                                        .map(n -> new NodeAndSubstitution(n, newSubstitution))
                                        .forEach(nodeAndSubsToVisit::add);
                            }
                        }
                    });

            if (optionalFocusNode.isPresent() && applicationResults.getOptionalTracker().isPresent()) {
                NodeTracker.NodeUpdate<N> nodeUpdate = applicationResults.getOptionalTracker().get()
                        .getUpdate(query, optionalFocusNode.get());
                optionalFocusNode = nodeUpdate.getNewNode();

                /**
                 * TODO: what should we do if it is replaced by its child?
                 */

                if (optionalFocusNode.isPresent() && ! treeComponent.contains(optionalFocusNode.get())) {
                    throw new IllegalStateException("Out-of-date ancestry tracker found");
                }
                /**
                 * Removed focus node
                 */
                if (!optionalFocusNode.isPresent()) {
                    optionalClosestAncestorOfFocusNode = applicationResults.getOptionalNewNode()
                            .map(Optional::of)
                            .orElseGet(applicationResults::getOptionalClosestAncestor);
                    optionalNextSiblingOfFocusNode = applicationResults.getOptionalNextSibling();
                }
            }
            /**
             * When a propagation is applied while the focus node has already been removed
             */
            else if (!optionalFocusNode.isPresent()) {
                // TODO: might be challenging to keep track of the next sibling
                throw new RuntimeException("TODO: handle the case where a propagation " +
                        "is applied while the focus node has already been removed");
            }


        }

        if (optionalFocusNode.isPresent()) {
            if (!treeComponent.contains(optionalFocusNode.get())) {
                throw new IllegalStateException("Out-dated focus node (its removal has not been detected)");
            }
            return new NodeTrackingResultsImpl<>(query, optionalFocusNode.get(), optionalAncestryTracker);
        }
        else {
            return new NodeTrackingResultsImpl<>(query, optionalNextSiblingOfFocusNode,
                    optionalClosestAncestorOfFocusNode, optionalAncestryTracker);
        }
    }
}
