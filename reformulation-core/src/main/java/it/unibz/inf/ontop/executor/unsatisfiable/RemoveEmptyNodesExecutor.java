package it.unibz.inf.ontop.executor.unsatisfiable;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.executor.NodeCentricInternalExecutor;
import it.unibz.inf.ontop.model.Constant;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.impl.EmptyNodeImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.proposal.*;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.NodeCentricOptimizationResultsImpl;

import java.util.Optional;

import static it.unibz.inf.ontop.executor.substitution.SubstitutionPropagationTools.propagateSubstitutionUp;
import static it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionTools.computeNullSubstitution;

/**
 * TODO: explain
 */
public class RemoveEmptyNodesExecutor implements NodeCentricInternalExecutor<EmptyNode, RemoveEmptyNodesProposal> {


    public static class ReactionResults {

        private final QueryNode closestAncestor;
        private final Optional<QueryNode> optionalNextSibling;

        public ReactionResults(IntermediateQuery resultingQuery, QueryNode closestAncestor,
                               Optional<QueryNode> optionalNextSibling) {
            this.closestAncestor = closestAncestor;
            this.optionalNextSibling = optionalNextSibling;

            /**
             * Checks the arguments
             */
            if (optionalNextSibling.isPresent()) {
                Optional<QueryNode> optionalParent = resultingQuery.getParent(optionalNextSibling.get());
                if ((!optionalParent.isPresent()) || optionalParent.get() != closestAncestor) {
                    throw new IllegalArgumentException("The closest ancestor must be the parent of the next sibling");
                }
            }
        }

        public QueryNode getClosestRemainingAncestor() {
            return closestAncestor;
        }

        public Optional<QueryNode> getOptionalNextSibling() {
            return optionalNextSibling;
        }
    }



    /**
     * TODO: explain
     */
    @Override
    public NodeCentricOptimizationResults<EmptyNode> apply(RemoveEmptyNodesProposal proposal, IntermediateQuery query,
                                                           QueryTreeComponent treeComponent)
            throws EmptyQueryException {

        // May update the query
        ReactionResults reactionResults = reactToEmptyChildNode(query, proposal.getFocusNode(), treeComponent);

        return new NodeCentricOptimizationResultsImpl<>(
                query,
                /**
                 * Next sibling (of the empty node or of the lastly removed ancestor)
                 */
                reactionResults.getOptionalNextSibling(),

                /**
                 * First ancestor to remain (may be updated)
                 */
                Optional.of(reactionResults.getClosestRemainingAncestor()));
    }

    /**
     * TODO: explain
     *
     * Recursive!
     */
    private static ReactionResults reactToEmptyChildNode(IntermediateQuery query, EmptyNode emptyNode,
                                                         QueryTreeComponent treeComponent)
            throws EmptyQueryException {

        QueryNode parentNode = query.getParent(emptyNode)
                // It is expected that the root has only one child, so if it is unsatisfiable,
                // this query will return empty results.
                .orElseThrow(EmptyQueryException::new);

        Optional<QueryNode> optionalNextSibling = query.getNextSibling(emptyNode);

        NodeTransformationProposal transformationProposal = parentNode.reactToEmptyChild(query, emptyNode);

        treeComponent.removeSubTree(emptyNode);

        switch (transformationProposal.getState()) {
            case NO_LOCAL_CHANGE:
                ImmutableSet<Variable> nullVariables = transformationProposal.getNullVariables();
                if (!nullVariables.isEmpty()) {
                    NodeCentricOptimizationResults<QueryNode> propagationResults =
                            applyNullPropagation(query, parentNode, treeComponent, nullVariables);

                    QueryNode closestAncestor = propagationResults.getOptionalNewNode()
                            .orElseGet(() -> propagationResults.getOptionalClosestAncestor()
                                    .orElseThrow(() -> new IllegalStateException(
                                            "If no ancestor remains, " +
                                                    "a EmptyQueryException should have been thrown")));


                    return new ReactionResults(query, closestAncestor,
                            // If the parent is still there, the sibling should not have been removed.
                            optionalNextSibling
                                    .filter(s -> propagationResults.getOptionalNewNode().isPresent())
                    );
                }
                else {
                    return new ReactionResults(query, parentNode, optionalNextSibling);
                }

            case REPLACE_BY_UNIQUE_NON_EMPTY_CHILD:
                return applyReplacementProposal(query, parentNode, optionalNextSibling, treeComponent,
                        transformationProposal, true);

            case REPLACE_BY_NEW_NODE:
                return applyReplacementProposal(query, parentNode, optionalNextSibling, treeComponent,
                        transformationProposal, false);

            case DECLARE_AS_EMPTY:
                EmptyNode newEmptyNode = new EmptyNodeImpl(transformationProposal.getNullVariables());
                treeComponent.replaceSubTree(parentNode, newEmptyNode);

                /**
                 * Recursive (cascade)
                 */
                return reactToEmptyChildNode(query, newEmptyNode, treeComponent);

            default:
                throw new RuntimeException("Unexpected state: " + transformationProposal.getState());
        }
    }

    private static NodeCentricOptimizationResults<QueryNode> applyNullPropagation(IntermediateQuery query,
                                                                                  QueryNode focusNode,
                                                                                  QueryTreeComponent treeComponent,
                                                                                  ImmutableSet<Variable> nullVariables)
            throws EmptyQueryException {

        ImmutableSubstitution<Constant> ascendingSubstitution = computeNullSubstitution(nullVariables);
        /**
         * Updates the tree component but does not affect the parent node and the (optional) next sibling.
         */
        return propagateSubstitutionUp(focusNode, ascendingSubstitution, query, treeComponent);
    }

    /**
     * TODO: explain
     *
     * TODO: clean
     */
    private static ReactionResults applyReplacementProposal(IntermediateQuery query,
                                                            QueryNode parentNode,
                                                            Optional<QueryNode> originalOptionalNextSibling,
                                                            QueryTreeComponent treeComponent,
                                                            NodeTransformationProposal transformationProposal,
                                                            boolean isReplacedByUniqueChild)
            throws EmptyQueryException {

        QueryNode replacingNode = transformationProposal.getOptionalNewNode()
                .orElseThrow(() -> new InvalidQueryOptimizationProposalException(
                        "Inconsistent transformation proposal: a replacing node must be given"));

        if (isReplacedByUniqueChild) {
            treeComponent.removeOrReplaceNodeByUniqueChildren(parentNode);
        }
        else {
            treeComponent.replaceNode(parentNode, replacingNode);
        }

        Optional<QueryNode> newOptionalNextSibling = isReplacedByUniqueChild
                /**
                 * Next sibling: the unique remaining child of the parent...
                 */
                ? Optional.of(replacingNode)
                /**
                 * ... or the same one (not touched)
                 */
                : originalOptionalNextSibling;

        ImmutableSet<Variable> nullVariables = transformationProposal.getNullVariables();
        if (nullVariables.isEmpty()) {
            QueryNode closestAncestor = isReplacedByUniqueChild
                    ? treeComponent.getParent(replacingNode)
                    .orElseThrow(() -> new InvalidQueryOptimizationProposalException(
                            "The root of the tree is not expected to be replaced."))
                    : replacingNode;

            return new ReactionResults(query, closestAncestor, newOptionalNextSibling);
        }
        else {
            NodeCentricOptimizationResults<QueryNode> propagationResults = applyNullPropagation(query, replacingNode,
                    treeComponent, nullVariables);

            Optional<QueryNode> optionalNewReplacingNode = propagationResults.getOptionalNewNode();

            if (optionalNewReplacingNode.isPresent()) {

                QueryNode closestAncestor = isReplacedByUniqueChild
                        ? query.getParent(optionalNewReplacingNode.get())
                        .orElseThrow(() -> new IllegalStateException("The root is not expected " +
                                "to be the replacing node"))
                        : optionalNewReplacingNode.get();

                return new ReactionResults(query, closestAncestor, newOptionalNextSibling);
            }
            else {
                QueryNode newAncestor = propagationResults.getOptionalClosestAncestor()
                        .orElseThrow(() -> new IllegalStateException("An ancestor was expected " +
                                "after ascendent substitution propagation (or an EmptyQueryException)"));
                // The parent has been removed so no more siblings
                return new ReactionResults(query, newAncestor, Optional.empty());
            }
        }
    }
}
