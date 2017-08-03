package it.unibz.inf.ontop.iq.executor.substitution;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.EmptyNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.SubstitutionResults;
import it.unibz.inf.ontop.iq.node.TrueNode;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.QueryTreeComponent;
import it.unibz.inf.ontop.iq.proposal.NodeTracker;
import it.unibz.inf.ontop.iq.proposal.NodeTrackingResults;
import it.unibz.inf.ontop.iq.proposal.RemoveEmptyNodeProposal;
import it.unibz.inf.ontop.iq.proposal.impl.NodeTrackingResultsImpl;
import it.unibz.inf.ontop.iq.proposal.impl.RemoveEmptyNodeProposalImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;

/**
 * TODO: explain
 */
public class LocalPropagationTools {

    /**
     * Results AFTER the substitution application.
     *
     * Only after propagating a substitution down
     *
     * An AncestryTracker only appears with empty nodes (and their removal)
     *
     */
    protected static class SubstitutionApplicationResults<N extends QueryNode>
            extends NodeTrackingResultsImpl<N> {
        /**
         * Substitution to propagate to this newNode
         */
        private final Optional<ImmutableSubstitution<? extends ImmutableTerm>> optionalSubst;
        private final boolean isReplacedByAChild;

        protected SubstitutionApplicationResults(IntermediateQuery query, N newNode,
                                                 Optional<ImmutableSubstitution<? extends ImmutableTerm>> optionalSubst,
                                                 Optional<NodeTracker> optionalTracker) {
            super(query, newNode, optionalTracker);
            this.optionalSubst = optionalSubst;
            this.isReplacedByAChild = false;
        }

        protected SubstitutionApplicationResults(IntermediateQuery query, QueryNode replacingNode,
                                                 Optional<ImmutableSubstitution<? extends ImmutableTerm>> optionalSubst,
                                                 boolean isReplacedByAChild, Optional<NodeTracker> optionalTracker) {
            super(query, Optional.of(replacingNode), optionalTracker);
            this.optionalSubst = optionalSubst;
            this.isReplacedByAChild = isReplacedByAChild;
        }

        /**
         * When the node has removed
         */
        protected SubstitutionApplicationResults(IntermediateQuery query, NodeTrackingResults<? extends QueryNode> emptyNodeResults) {
            super(query,
                    emptyNodeResults.getOptionalNextSibling(),
                    emptyNodeResults.getOptionalClosestAncestor(), emptyNodeResults.getOptionalTracker());
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
     * Applies the substitution to the newNode and its replacing child (if any)
     *
     * An AncestryTracker is only created in case of empty nodes (they are immediately removed).
     *
     */
    protected static <N extends QueryNode>
    SubstitutionApplicationResults<N> applySubstitutionToNode(N node, ImmutableSubstitution substitution,
                                                              IntermediateQuery query, QueryTreeComponent treeComponent,
                                                              Optional<NodeTracker> optionalTracker)
            throws EmptyQueryException {
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
                optionalTracker.ifPresent(tr -> tr.recordReplacement(node, newNode));
                treeComponent.replaceNode(node, newNode);

                return new SubstitutionApplicationResults<>(query, newNode, newSubstitution, optionalTracker);

            case NO_CHANGE:
                return new SubstitutionApplicationResults<>(query, node, newSubstitution, optionalTracker);

            case REPLACE_BY_CHILD:
                QueryNode replacingChild = substitutionResults.getOptionalReplacingChildPosition()
                        .flatMap(position -> query.getChild(node, position))
                        .orElseGet(() -> query.getFirstChild(node)
                                .orElseThrow(() -> new IllegalStateException("No replacing child is available")));
                optionalTracker.ifPresent(tr -> tr.recordUpcomingReplacementByChild(query, node, replacingChild));

                treeComponent.replaceNodeByChild(node,
                        substitutionResults.getOptionalReplacingChildPosition());

                return new SubstitutionApplicationResults<>(query, replacingChild, newSubstitution, true, optionalTracker);

            case DECLARE_AS_TRUE:
                TrueNode replacingNode = query.getFactory().createTrueNode();
                // The replaced node must already be a leaf,
                // so we can perform a single node replacement
                // (there is no need here to delete its subtree)
                optionalTracker.ifPresent(tr -> tr.recordReplacement(node, replacingNode));
                treeComponent.replaceNode(node,replacingNode);

                return new SubstitutionApplicationResults<>(query, replacingNode, newSubstitution, false, optionalTracker);

            case INSERT_CONSTRUCTION_NODE:
                throw new IllegalStateException("Construction newNode insertion not expected " +
                        "while pushing a substitution down");
                /**
                 * Replace the sub-tree by an empty newNode
                 */
            case DECLARE_AS_EMPTY:
                EmptyNode replacingEmptyNode = replaceByEmptyNode(node, substitution, query, optionalTracker);
                treeComponent.replaceSubTree(node, replacingEmptyNode);

                RemoveEmptyNodeProposal removalProposal = optionalTracker.isPresent()
                        ? new RemoveEmptyNodeProposalImpl(replacingEmptyNode, optionalTracker.get())
                        : new RemoveEmptyNodeProposalImpl(replacingEmptyNode, true);

                // May restructure significantly the query
                NodeTrackingResults<EmptyNode> removeEmptyNodeResults = query.applyProposal(removalProposal, true);

                return new SubstitutionApplicationResults<>(query, removeEmptyNodeResults);

            default:
                throw new IllegalStateException("Unknown local action: " + substitutionResults.getLocalAction());
        }
    }

    private static EmptyNode replaceByEmptyNode(QueryNode rejectedNode, ImmutableSubstitution descendingSubstitution,
                                                IntermediateQuery query, Optional<NodeTracker> optionalTracker) {
        optionalTracker.ifPresent(tr -> tr.recordUpcomingRemoval(query, rejectedNode));
        /**
         * The new set of projected variables have to take into account
         * the changes proposed by the descending substitution.
         */
        ImmutableSet<Variable> newProjectedVariables = query.getVariables(rejectedNode).stream()
                .flatMap(v -> descendingSubstitution.apply(v).getVariableStream())
                .collect(ImmutableCollectors.toSet());

        return query.getFactory().createEmptyNode(newProjectedVariables);
    }

}
