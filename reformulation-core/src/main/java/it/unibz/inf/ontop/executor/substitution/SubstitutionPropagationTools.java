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
    protected static class SubstitutionApplicationResults<N extends QueryNode> {
        private final Optional<N> newNode;
        /**
         * Substitution to propagate to this newNode
         */
        private final Optional<ImmutableSubstitution<? extends ImmutableTerm>> optionalSubst;
        private final boolean isReplacedByAChild;
        private final Optional<QueryNode> replacingNode;

        protected SubstitutionApplicationResults(N newNode,
                                                 Optional<ImmutableSubstitution<? extends ImmutableTerm>> optionalSubst) {
            this.newNode = Optional.of(newNode);
            this.replacingNode = Optional.empty();
            this.optionalSubst = optionalSubst;
            this.isReplacedByAChild = false;
        }

        protected SubstitutionApplicationResults(QueryNode replacingNode,
                                                 Optional<ImmutableSubstitution<? extends ImmutableTerm>> optionalSubst,
                                                 boolean isReplacedByAChild) {
            this.newNode = Optional.empty();
            this.replacingNode = Optional.of(replacingNode);
            this.optionalSubst = optionalSubst;
            this.isReplacedByAChild = isReplacedByAChild;
        }

        public Optional<N> getNewNode() {
            return newNode;
        }

        public Optional<ImmutableSubstitution<? extends ImmutableTerm>> getOptionalSubstitution() {
            return optionalSubst;
        }

        public boolean isReplacedByAChild() {
            return isReplacedByAChild;
        }

        public Optional<QueryNode> getReplacingNode() {
            return replacingNode;
        }

        public QueryNode getNewOrReplacingNode() {
            return newNode
                    .map(Optional::<QueryNode>of)
                    .orElse(replacingNode)
                    .orElseThrow(() -> new IllegalStateException("At least one query node must be present"));
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
    public static QueryTreeComponent propagateSubstitutionDown(final QueryNode focusNode,
                                                               final ImmutableSubstitution<? extends ImmutableTerm> initialSubstitutionToPropagate,
                                                               final IntermediateQuery query,
                                                               final QueryTreeComponent treeComponent)
            throws QueryNodeSubstitutionException {

        return propagateSubstitutionDownToNodes(treeComponent.getChildrenStream(focusNode),
                initialSubstitutionToPropagate, query, treeComponent);
    }

    /**
     * Applies the substitution to the starting nodes and to their childre
     */
    private static QueryTreeComponent propagateSubstitutionDownToNodes(final Stream<QueryNode> startingNodes,
                                                               final ImmutableSubstitution<? extends ImmutableTerm> initialSubstitutionToPropagate,
                                                               final IntermediateQuery query,
                                                               final QueryTreeComponent treeComponent)
            throws QueryNodeSubstitutionException {

        Queue<NodeAndSubstitution> nodeAndSubsToVisit = new LinkedList<>();
        startingNodes
                .map(n -> new NodeAndSubstitution(n, initialSubstitutionToPropagate))
                .forEach(nodeAndSubsToVisit::add);

        while (!nodeAndSubsToVisit.isEmpty()) {
            NodeAndSubstitution initialNodeAndSubstitution = nodeAndSubsToVisit.poll();

            SubstitutionApplicationResults<QueryNode> applicationResults = applySubstitutionToNode(
                    initialNodeAndSubstitution.node,
                    initialNodeAndSubstitution.substitution, query, treeComponent);

            /**
             * Adds the children - new substitution pairs to the queue
             */
            applicationResults.getOptionalSubstitution()
                    .ifPresent(newSubstitution -> {
                        if (applicationResults.isReplacedByAChild) {
                            nodeAndSubsToVisit.add(new NodeAndSubstitution(applicationResults.getReplacingNode().get(),
                                    newSubstitution));
                        }
                        else {
                            treeComponent.getChildren(applicationResults.getNewOrReplacingNode()).stream()
                                    .map(n -> new NodeAndSubstitution(n, newSubstitution))
                                    .forEach(nodeAndSubsToVisit::add);
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
                                                                            QueryTreeComponent treeComponent) {
        SubstitutionResults<? extends QueryNode> substitutionResults = node.applyDescendingSubstitution(substitution, query);

        Optional<? extends ImmutableSubstitution<? extends ImmutableTerm>> newSubstitution =
                substitutionResults.getSubstitutionToPropagate();

        switch (substitutionResults.getLocalAction()) {
            case NEW_NODE:
                N newNode = (N) substitutionResults.getOptionalNewNode()
                        .orElseThrow(() -> new IllegalStateException("A new newNode was expected"));
                if (newNode == node) {
                    throw new IllegalStateException("NEW_NODE action must not return the same node. " +
                            "Use NO_CHANGE instead.");
                }
                treeComponent.replaceNode(node, newNode);
                return new SubstitutionApplicationResults(newNode, newSubstitution);

            case NO_CHANGE:
                return new SubstitutionApplicationResults(node, newSubstitution);

            case REPLACE_BY_CHILD:
                QueryNode replacingChild = substitutionResults.getOptionalReplacingChildPosition()
                        .flatMap(position -> query.getChild(node, position))
                        .orElseGet(() -> query.getFirstChild(node)
                                .orElseThrow(() -> new IllegalStateException("No replacing child is available")));

                treeComponent.replaceNodeByChild(node,
                        substitutionResults.getOptionalReplacingChildPosition());

                return new SubstitutionApplicationResults(replacingChild, newSubstitution, true);

            case INSERT_CONSTRUCTION_NODE:
                throw new IllegalStateException("Construction newNode insertion not expected " +
                        "while pushing a substitution down");
                /**
                 * Replace the sub-tree by an empty newNode
                 */
            case DECLARE_AS_EMPTY:
                QueryNode replacingNode = replaceByEmptyNode(node, substitution, query);
                treeComponent.replaceSubTree(node, replacingNode);

                return new SubstitutionApplicationResults(replacingNode, newSubstitution, false);

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
             * Applies the substitution and analyses the results
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
                 * Ancestor is empty --> applies a ReactToChildDeletionProposal and returns the remaining ancestor
                 */
                case DECLARE_AS_EMPTY:
                    /**
                     *
                     */
                    NodeCentricOptimizationResults<EmptyNode> removalResults =
                            reactToEmptinessDeclaration(query, currentAncestor, treeComponent);
                    // TODO: make sure it makes sense
                    return new NodeCentricOptimizationResultsImpl<T>(query, removalResults.getOptionalNextSibling(),
                            removalResults.getOptionalClosestAncestor());
                default:
                    throw new IllegalStateException("Unknown local action: " + substitutionResults.getLocalAction());
            }

            Optional<? extends ImmutableSubstitution<? extends ImmutableTerm>> optionalNewSubstitution =
                    substitutionResults.getSubstitutionToPropagate();

            /**
             * Propagates the substitution DOWN to the other children
             */
            optionalNewSubstitution.ifPresent(subst -> propagateSubstitutionDownToNodes(otherChildren, subst,
                    query, treeComponent));

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

        RemoveEmptyNodesProposal proposal = new RemoveEmptyNodesProposalImpl(replacingEmptyNode);
        return query.applyProposal(proposal, true);
    }
}
