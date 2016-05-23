package it.unibz.inf.ontop.executor.deletion;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.executor.InternalProposalExecutor;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition;
import it.unibz.inf.ontop.pivotalrepr.impl.QueryTreeComponent;
import it.unibz.inf.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import it.unibz.inf.ontop.pivotalrepr.proposal.ReactToChildDeletionProposal;
import it.unibz.inf.ontop.pivotalrepr.proposal.ReactToChildDeletionResults;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.ReactToChildDeletionResultsImpl;

import java.util.Optional;

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
                /**
                 * TODO: handle nulls
                 */
                return new ReactToChildDeletionResultsImpl(query, parentNode, optionalNextSibling);

            case REPLACE_BY_UNIQUE_CHILD:
                return applyReplacementProposal(query, parentNode, treeComponent, transformationProposal, true);

            case REPLACE_BY_NEW_NODE:
                return applyReplacementProposal(query, parentNode, treeComponent, transformationProposal, false);

            case DELETE:
                return applyDeletionProposal(query, parentNode, treeComponent, transformationProposal.getNullVariables());

            default:
                throw new RuntimeException("Unexpected state: " + transformationProposal.getState());
        }
    }

    /**
     * TODO: handle nulls
     */
    private static ReactToChildDeletionResults applyReplacementProposal(IntermediateQuery query,
                                                                        QueryNode parentNode,
                                                                        QueryTreeComponent treeComponent,
                                                                        NodeTransformationProposal transformationProposal,
                                                                        boolean isReplacedByUniqueChild) {
        Optional<QueryNode> optionalGrandParent = treeComponent.getParent(parentNode);

        if (!optionalGrandParent.isPresent()) {
            throw new InvalidQueryOptimizationProposalException("The root of the tree is not expected to be replaced.");
        }

        Optional<QueryNode> optionalReplacingNode = transformationProposal.getOptionalNewNode();
        if (!optionalReplacingNode.isPresent()) {
            throw new InvalidQueryOptimizationProposalException("Inconsistent transformation proposal: a replacing node must be given");
        }
        if (isReplacedByUniqueChild) {
            treeComponent.removeOrReplaceNodeByUniqueChildren(parentNode);
        }
        else {
            treeComponent.replaceNode(parentNode, optionalReplacingNode.get());
        }

        /**
         * Next sibling: only when the parent is replaced by its unique remaining child.
         */
        Optional<QueryNode> optionalNextSibling;
        if (isReplacedByUniqueChild) {
            optionalNextSibling = optionalReplacingNode;
        }
        else {
            optionalNextSibling = Optional.empty();
        }
        return new ReactToChildDeletionResultsImpl(query, optionalGrandParent.get(), optionalNextSibling);
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
