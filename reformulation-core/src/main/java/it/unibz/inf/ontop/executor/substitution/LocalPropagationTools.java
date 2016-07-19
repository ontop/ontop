package it.unibz.inf.ontop.executor.substitution;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.EmptyNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.proposal.AncestryTrackingResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.RemoveEmptyNodeProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.AncestryTrackingResultsImpl;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.RemoveEmptyNodeProposalImpl;
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
            extends AncestryTrackingResultsImpl<N> {
        /**
         * Substitution to propagate to this newNode
         */
        private final Optional<ImmutableSubstitution<? extends ImmutableTerm>> optionalSubst;
        private final boolean isReplacedByAChild;

        protected SubstitutionApplicationResults(IntermediateQuery query, N newNode,
                                                 Optional<ImmutableSubstitution<? extends ImmutableTerm>> optionalSubst) {
            super(query, newNode, Optional.empty());
            this.optionalSubst = optionalSubst;
            this.isReplacedByAChild = false;
        }

        protected SubstitutionApplicationResults(IntermediateQuery query, QueryNode replacingNode,
                                                 Optional<ImmutableSubstitution<? extends ImmutableTerm>> optionalSubst,
                                                 boolean isReplacedByAChild) {
            super(query, Optional.of(replacingNode), Optional.empty());
            this.optionalSubst = optionalSubst;
            this.isReplacedByAChild = isReplacedByAChild;
        }

        /**
         * When the node has removed
         */
        protected SubstitutionApplicationResults(N originalFocusNode, AncestryTrackingResults<EmptyNode> emptyNodeResults) {
            super(emptyNodeResults.getResultingQuery(),
                    emptyNodeResults.getOptionalNextSibling(),
                    emptyNodeResults.getOptionalClosestAncestor(), emptyNodeResults.getOptionalTracker());
            this.optionalSubst = Optional.empty();
            this.isReplacedByAChild = false;
            // TODO:
        }

        public Optional<ImmutableSubstitution<? extends ImmutableTerm>> getOptionalSubstitution() {
            return optionalSubst;
        }

        public boolean isReplacedByAChild() {
            return isReplacedByAChild;
        }
    }

    /**
     * Applies the substitution to the newNode
     *
     * An AncestryTracker is only created in case of empty nodes (they are immediately removed).
     *
     */
    protected static <N extends QueryNode>
    SubstitutionApplicationResults<N> applySubstitutionToNode(N node, ImmutableSubstitution substitution,
                                                              IntermediateQuery query, QueryTreeComponent treeComponent)
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

                RemoveEmptyNodeProposal removalProposal = new RemoveEmptyNodeProposalImpl(replacingEmptyNode, true);

                // May restructure significantly the query
                AncestryTrackingResults<EmptyNode> removeEmptyNodeResults = query.applyProposal(removalProposal, true);

                return new SubstitutionApplicationResults<>(node, removeEmptyNodeResults);

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

}
