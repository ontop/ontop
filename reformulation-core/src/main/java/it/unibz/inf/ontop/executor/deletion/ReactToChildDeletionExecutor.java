package it.unibz.inf.ontop.executor.deletion;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.executor.InternalProposalExecutor;
import it.unibz.inf.ontop.model.Constant;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.proposal.*;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.ReactToChildDeletionResultsImpl;

import java.util.Optional;

import static it.unibz.inf.ontop.executor.substitution.SubstitutionPropagationTools.propagateSubstitutionUp;
import static it.unibz.inf.ontop.owlrefplatform.core.basicoperations.ImmutableSubstitutionTools.computeNullSubstitution;

/**
 * TODO: explain
 */
public class ReactToChildDeletionExecutor implements InternalProposalExecutor<ReactToChildDeletionProposal,
        ReactToChildDeletionResults> {
    @Override
    public ReactToChildDeletionResults apply(ReactToChildDeletionProposal proposal, IntermediateQuery query,
                                 QueryTreeComponent treeComponent) throws InvalidQueryOptimizationProposalException, EmptyQueryException {

        // May alter the query and its tree component
        return analyzeAndUpdate(query,
                proposal.getParentNode(),
                proposal.getOptionalPositionOfDeletedChild(),
                proposal.getVariablesProjectedByDeletedChild(),
                proposal.getOptionalNextSibling(),
                treeComponent);
    }

    /**
     * TODO: explain
     *
     * Recursive!
     */
    private static ReactToChildDeletionResults analyzeAndUpdate(IntermediateQuery query, QueryNode parentNode,
                                                                Optional<ArgumentPosition> optionalPositionOfDeletedChild,
                                                                ImmutableSet<Variable> variablesProjectedByDeletedChild,
                                                                Optional<QueryNode> optionalNextSibling,
                                                                QueryTreeComponent treeComponent)
            throws EmptyQueryException {
        ReactToChildDeletionTransformer transformer = new ReactToChildDeletionTransformer(query,
                optionalPositionOfDeletedChild, variablesProjectedByDeletedChild);

        NodeTransformationProposal transformationProposal = parentNode.acceptNodeTransformer(transformer);

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

            case DELETE:
                return applyDeletionProposal(query, parentNode, treeComponent, transformationProposal.getNullVariables());

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

    private static ReactToChildDeletionResults applyDeletionProposal(IntermediateQuery query, QueryNode parentNode,
                                                                     QueryTreeComponent treeComponent,
                                                                     ImmutableSet<Variable> nullVariables)
            throws EmptyQueryException {
        Optional<QueryNode> optionalGrandParent = query.getParent(parentNode);
        Optional<ArgumentPosition> optionalPosition = query.getOptionalPosition(parentNode);
        Optional<QueryNode> optionalNextSibling = query.getNextSibling(parentNode);

        treeComponent.removeSubTree(parentNode);

        /**
         * Recursive (cascade)
         */
        if (optionalGrandParent.isPresent()) {
            return analyzeAndUpdate(query, optionalGrandParent.get(), optionalPosition, nullVariables,
                    optionalNextSibling, treeComponent);
        }
        /**
         * Arrived to the root
         */
        else {
            throw new EmptyQueryException();
        }
    }
}
