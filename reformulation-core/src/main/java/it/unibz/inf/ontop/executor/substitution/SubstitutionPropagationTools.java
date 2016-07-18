package it.unibz.inf.ontop.executor.substitution;


import java.util.Optional;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.EmptyNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.proposal.NodeCentricOptimizationResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.RemoveEmptyNodesProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.RemoveEmptyNodesProposalImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Stream;

/**
 * These methods are only accessible by InternalProposalExecutors (requires access to the QueryTreeComponent).
 */
public class SubstitutionPropagationTools {

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
     * Results AFTER the substitution application.
     *
     * Only after propagating a substitution down
     */
    protected static class SubstitutionApplicationResults<N extends QueryNode>
            extends NodeCentricOptimizationResultsImpl<N> {
        /**
         * Substitution to propagate to this newNode
         */
        private final Optional<ImmutableSubstitution<? extends ImmutableTerm>> optionalSubst;
        private final boolean isReplacedByAChild;

        protected SubstitutionApplicationResults(IntermediateQuery query, N newNode,
                                                 Optional<ImmutableSubstitution<? extends ImmutableTerm>> optionalSubst) {
            super(query, newNode);
            this.optionalSubst = optionalSubst;
            this.isReplacedByAChild = false;
        }

        protected SubstitutionApplicationResults(IntermediateQuery query, QueryNode replacingNode,
                                                 Optional<ImmutableSubstitution<? extends ImmutableTerm>> optionalSubst,
                                                 boolean isReplacedByAChild) {
            super(query, Optional.of(replacingNode));
            this.optionalSubst = optionalSubst;
            this.isReplacedByAChild = isReplacedByAChild;
        }

        /**
         * When the node has removed
         */
        protected SubstitutionApplicationResults(IntermediateQuery query,
                                                 Optional<QueryNode> optionalNextSibling,
                                                 Optional<QueryNode> optionalClosestAncestor) {
            super(query, optionalNextSibling, optionalClosestAncestor);
            this.optionalSubst = Optional.empty();
            this.isReplacedByAChild = false;
        }

        public Optional<ImmutableSubstitution<? extends ImmutableTerm>> getOptionalSubstitution() {
            return optionalSubst;
        }

        public boolean isReplacedByAChild() {
            return isReplacedByAChild;
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
    public static <N extends QueryNode> NodeCentricOptimizationResults<N> propagateSubstitutionDown(final N focusNode,
                                                               final ImmutableSubstitution<? extends ImmutableTerm> initialSubstitutionToPropagate,
                                                               final IntermediateQuery query,
                                                               final QueryTreeComponent treeComponent)
            throws QueryNodeSubstitutionException, EmptyQueryException {

        return propagateSubstitutionDownToNodes(focusNode, treeComponent.getChildrenStream(focusNode),
                initialSubstitutionToPropagate, query, treeComponent);
    }

    /**
     * Applies the substitution to the starting nodes and to their children
     */
    private static <N extends QueryNode> NodeCentricOptimizationResults<N> propagateSubstitutionDownToNodes(
            N focusNode, final Stream<QueryNode> startingNodes,
            final ImmutableSubstitution<? extends ImmutableTerm> initialSubstitutionToPropagate,
            final IntermediateQuery query, final QueryTreeComponent treeComponent)
            throws QueryNodeSubstitutionException, EmptyQueryException {

        Queue<NodeAndSubstitution> nodeAndSubsToVisit = new LinkedList<>();
        startingNodes
                .map(n -> new NodeAndSubstitution(n, initialSubstitutionToPropagate))
                .forEach(nodeAndSubsToVisit::add);

        while (!nodeAndSubsToVisit.isEmpty()) {
            NodeAndSubstitution initialNodeAndSubstitution = nodeAndSubsToVisit.poll();

            /**
             * Some nodes may have be removed in the reaction to the removal of an empty sibling.
             *
             * It is ASSUMED that the removal of a child:
             *    - either leads to the removal of some other siblings
             *    - or have no impact on its siblings beside moving their position (e.g. making them replace their parent).
             *
             * If this assumption holds, it is then safe to ignore a removed node.
             *
             */
            if (!treeComponent.contains(initialNodeAndSubstitution.node)) {
                break;
            }

            SubstitutionApplicationResults<QueryNode> applicationResults = applySubstitutionToNode(
                    initialNodeAndSubstitution.node,
                    initialNodeAndSubstitution.substitution, query, treeComponent);

            /**
             * Adds the children - new substitution pairs to the queue
             */
            applicationResults.getOptionalSubstitution()
                    .ifPresent(newSubstitution -> {
                        if (applicationResults.isReplacedByAChild) {
                            nodeAndSubsToVisit.add(new NodeAndSubstitution(applicationResults.getOptionalReplacingChild().get(),
                                    newSubstitution));
                        }
                        else {
                            applicationResults.getNewNodeOrReplacingChild()
                                    .ifPresent(newNode -> treeComponent.getChildren(newNode).stream()
                                            .map(n -> new NodeAndSubstitution(n, newSubstitution))
                                            .forEach(nodeAndSubsToVisit::add));
                        }
                    });
        }
        return treeComponent;
    }

    /**
     * Applies the substitution to the newNode
     */
    protected static <N extends QueryNode> SubstitutionApplicationResults<N> applySubstitutionToNode(N node, ImmutableSubstitution substitution,
                                                                            IntermediateQuery query,
                                                                            QueryTreeComponent treeComponent) throws EmptyQueryException {
        SubstitutionResults<? extends QueryNode> substitutionResults = node.applyDescendingSubstitution(substitution, query);

        Optional<ImmutableSubstitution<? extends ImmutableTerm>> newSubstitution =
                substitutionResults.getSubstitutionToPropagate()
                        .map(s -> (ImmutableSubstitution<? extends ImmutableTerm>)s);

        switch (substitutionResults.getLocalAction()) {
            case NEW_NODE:
                N newNode = (N) substitutionResults.getOptionalNewNode()
                        .orElseThrow(() -> new IllegalStateException("A new newNode was expected"));
                if (newNode == node) {
                    throw new IllegalStateException("NEW_NODE action must not return the same node. " +
                            "Use NO_CHANGE instead.");
                }
                treeComponent.replaceNode(node, newNode);
                return new SubstitutionApplicationResults<>(query, newNode, newSubstitution);

            case NO_CHANGE:
                return new SubstitutionApplicationResults<>(query, node, newSubstitution);

            case REPLACE_BY_CHILD:
                QueryNode replacingChild = substitutionResults.getOptionalReplacingChildPosition()
                        .flatMap(position -> query.getChild(node, position))
                        .orElseGet(() -> query.getFirstChild(node)
                                .orElseThrow(() -> new IllegalStateException("No replacing child is available")));

                treeComponent.replaceNodeByChild(node,
                        substitutionResults.getOptionalReplacingChildPosition());

                return new SubstitutionApplicationResults<>(query, replacingChild, newSubstitution, true);

            case INSERT_CONSTRUCTION_NODE:
                throw new IllegalStateException("Construction newNode insertion not expected " +
                        "while pushing a substitution down");
                /**
                 * Replace the sub-tree by an empty newNode
                 */
            case DECLARE_AS_EMPTY:
                EmptyNode replacingEmptyNode = replaceByEmptyNode(node, substitution, query);
                treeComponent.replaceSubTree(node, replacingEmptyNode);

                /**
                 * Immediately removes the empty node
                 */
                RemoveEmptyNodesProposal cleaningProposal = new RemoveEmptyNodesProposalImpl(replacingEmptyNode, true);
                // May restructure significantly the query
                NodeCentricOptimizationResults<EmptyNode> cleaningResults = query.applyProposal(cleaningProposal, true);

                return new SubstitutionApplicationResults<>(query,
                        cleaningResults.getOptionalNextSibling(),
                        cleaningResults.getOptionalClosestAncestor());

            default:
                throw new IllegalStateException("Unknown local action: " + substitutionResults.getLocalAction());
        }
    }

    private static EmptyNode replaceByEmptyNode(QueryNode rejectingNode, ImmutableSubstitution descendingSubstitution,
                                                IntermediateQuery query) {
        /**
         * The new set of projected variables have to take into account
         * the changes proposed by the descending substitution.
         */
        ImmutableSet<Variable> newProjectedVariables = query.getProjectedVariables(rejectingNode).stream()
                .flatMap(v -> descendingSubstitution.apply(v).getVariableStream())
                .collect(ImmutableCollectors.toSet());

        return new EmptyNodeImpl(newProjectedVariables);
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
    public static <T extends QueryNode> NodeCentricOptimizationResults<T> propagateSubstitutionUp(
            T focusNode, ImmutableSubstitution<? extends ImmutableTerm> substitutionToPropagate,
            IntermediateQuery query, QueryTreeComponent treeComponent) throws QueryNodeSubstitutionException,
            EmptyQueryException {

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

                    otherChildren = ancestorChild != replacingChild
                            ? Stream.of(replacingChild)
                            : Stream.of();
                    futureChild = replacingChild;
                    break;
                /**
                 * Ancestor is empty --> removes it and returns the closest ancestor + the next sibling
                 */
                case DECLARE_AS_EMPTY:
                    NodeCentricOptimizationResults<EmptyNode> removalResults =
                            reactToEmptinessDeclaration(query, currentAncestor, treeComponent);
                    return new NodeCentricOptimizationResultsImpl<>(query, removalResults.getOptionalNextSibling(),
                            removalResults.getOptionalClosestAncestor());
                default:
                    throw new IllegalStateException("Unknown local action: " + substitutionResults.getLocalAction());
            }

            Optional<? extends ImmutableSubstitution<? extends ImmutableTerm>> optionalNewSubstitution =
                    substitutionResults.getSubstitutionToPropagate();

            /**
             * Propagates the substitution DOWN to the other children
             */
            if (optionalNewSubstitution.isPresent()) {
                propagateSubstitutionDownToNodes(futureChild, otherChildren, optionalNewSubstitution.get(), query, treeComponent);
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

        /**
         * If no ancestor has been
         */
        return new NodeCentricOptimizationResultsImpl<>(query, focusNode);
    }

    /**
     * Returns results centered on the removed node.
     */
    private static NodeCentricOptimizationResults<EmptyNode> reactToEmptinessDeclaration(
            IntermediateQuery query, QueryNode currentAncestor, QueryTreeComponent treeComponent) throws EmptyQueryException {

        ImmutableSet<Variable> nullVariables = query.getProjectedVariables(currentAncestor);
        EmptyNode replacingEmptyNode = new EmptyNodeImpl(nullVariables);

        treeComponent.replaceSubTree(currentAncestor, replacingEmptyNode);

        RemoveEmptyNodesProposal proposal = new RemoveEmptyNodesProposalImpl(replacingEmptyNode, false);
        return query.applyProposal(proposal, true);
    }
}
