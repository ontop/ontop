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
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.ReactToChildDeletionResultsImpl;

import java.util.Optional;

import static it.unibz.inf.ontop.executor.substitution.SubstitutionPropagationTools.propagateSubstitutionUp;
import static it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionTools.computeNullSubstitution;

/**
 * TODO: explain
 */
public class RemoveEmptyNodesExecutor implements NodeCentricInternalExecutor<EmptyNode, RemoveEmptyNodesProposal> {

    /**
     * TODO: explain
     */
    @Override
    public NodeCentricOptimizationResults<EmptyNode> apply(RemoveEmptyNodesProposal proposal, IntermediateQuery query,
                                                           QueryTreeComponent treeComponent)
            throws EmptyQueryException {

        return removeFocusEmptyNode(proposal.getFocusNode(), query, treeComponent);
    }

    /**
     * When the focus node is an EmptyNode (special case)
     */
    private NodeCentricOptimizationResults<EmptyNode> removeFocusEmptyNode(EmptyNode emptyFocusNode, IntermediateQuery query,
                                                                   QueryTreeComponent treeComponent)
            throws EmptyQueryException {

        // May update the query
        ReactToChildDeletionResults reactionResults = analyzeAndUpdate(query, emptyFocusNode, treeComponent);

        return new NodeCentricOptimizationResultsImpl<>(
                query,
                reactionResults.getOptionalNextSibling(),
                Optional.of(reactionResults.getClosestRemainingAncestor()));
    }

    /**
     * TODO: explain
     *
     * Recursive!
     */
    private static ReactToChildDeletionResults analyzeAndUpdate(IntermediateQuery query, EmptyNode emptyNode,
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


                    return new ReactToChildDeletionResultsImpl(query, closestAncestor,
                            // If the parent is still there, the sibling should not have been removed.
                            optionalNextSibling
                                    .filter(s -> propagationResults.getOptionalNewNode().isPresent())
                    );
                }
                else {
                    return new ReactToChildDeletionResultsImpl(query, parentNode, optionalNextSibling);
                }

            case REPLACE_BY_UNIQUE_CHILD:
                return applyReplacementProposal(query, parentNode, optionalNextSibling, treeComponent,
                        transformationProposal, true);

            case REPLACE_BY_NEW_NODE:
                return applyReplacementProposal(query, parentNode, optionalNextSibling, treeComponent,
                        transformationProposal, false);

            case DECLARE_AS_EMPTY:
                return declareAsEmpty(query, parentNode, treeComponent, transformationProposal.getNullVariables());

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
    private static ReactToChildDeletionResults applyReplacementProposal(IntermediateQuery query,
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

            return new ReactToChildDeletionResultsImpl(query, closestAncestor, newOptionalNextSibling);
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

                return new ReactToChildDeletionResultsImpl(query, closestAncestor, newOptionalNextSibling);
            }
            else {
                QueryNode newAncestor = propagationResults.getOptionalClosestAncestor()
                        .orElseThrow(() -> new IllegalStateException("An ancestor was expected " +
                                "after ascendent substitution propagation (or an EmptyQueryException)"));
                // The parent has been removed so no more siblings
                return new ReactToChildDeletionResultsImpl(query, newAncestor, Optional.empty());
            }
        }
    }

    private static ReactToChildDeletionResults declareAsEmpty(IntermediateQuery query, QueryNode parentNode,
                                                              QueryTreeComponent treeComponent,
                                                              ImmutableSet<Variable> nullVariables)
            throws EmptyQueryException {
        EmptyNode newEmptyNode = new EmptyNodeImpl(nullVariables);
        treeComponent.replaceSubTree(parentNode, newEmptyNode);

        /**
         * Recursive (cascade)
         */
        return analyzeAndUpdate(query, newEmptyNode, treeComponent);
    }

}
