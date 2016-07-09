package it.unibz.inf.ontop.pivotalrepr.impl;

import java.util.Optional;
import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.executor.InternalProposalExecutor;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.VariableOrGroundTerm;
import it.unibz.inf.ontop.model.ImmutableSubstitution;
import it.unibz.inf.ontop.pivotalrepr.proposal.*;
import it.unibz.inf.ontop.pivotalrepr.proposal.impl.ProposalResultsImpl;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.transformer.BindingTransferTransformer;
import it.unibz.inf.ontop.pivotalrepr.transformer.impl.BasicBindingTransferTransformer;

import java.util.LinkedList;
import java.util.Queue;

/**
 * TODO: explain
 */
public class SubstitutionLiftProposalExecutor implements InternalProposalExecutor<SubstitutionLiftProposal, ProposalResults> {

    @Override
    public ProposalResults apply(SubstitutionLiftProposal proposal, IntermediateQuery query, QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException {
        for (BindingTransfer bindingTransfer : proposal.getBindingTransfers()) {
            applyBindingTransfer(bindingTransfer, treeComponent);
        }

        try {
            for (ConstructionNodeUpdate update : proposal.getNodeUpdates()) {
                applyConstructionNodeUpdate(update, query, treeComponent);
            }
        } catch (QueryNodeSubstitutionException e) {
            // TODO: should we throw an InvalidProposalException instead?
            throw e;
        }

        return new ProposalResultsImpl(query);
    }

    /**
     * TODO: explain
     */
    private void applyBindingTransfer(BindingTransfer bindingTransfer, QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException {
        ConstructionNode targetNode = bindingTransfer.getTargetNode();

        for (ConstructionNode sourceNode : bindingTransfer.getSourceNodes()) {
            ImmutableList<QueryNode> ancestors;
            try {
                ancestors = treeComponent.getAncestors(sourceNode);
            } catch (IllegalTreeException e) {
                throw new InvalidQueryOptimizationProposalException("The source node " + sourceNode + " is not ");
            }
            if (!ancestors.contains(targetNode)) {
                throw new InvalidQueryOptimizationProposalException("The target node " + targetNode
                        + " is not an ancestor of " + sourceNode);
            }

            /**
             * Updates the ancestors between the source and the target.
             */
            BindingTransferTransformer transformer = new BasicBindingTransferTransformer(bindingTransfer);
            for (QueryNode ancestor : ancestors) {
                if (ancestor == targetNode) {
                    break;
                }

                try {
                    QueryNode newAncestor = ancestor.acceptNodeTransformer(transformer);
                    if (!newAncestor.equals(ancestor)) {
                        treeComponent.replaceNode(ancestor, newAncestor);
                    }

                } catch (QueryNodeTransformationException e) {
                    throw new InvalidQueryOptimizationProposalException(e.getMessage());
                } catch (NotNeededNodeException e) {
                    try {
                        treeComponent.removeOrReplaceNodeByUniqueChildren(ancestor);
                    } catch (IllegalTreeUpdateException e1) {
                        throw new InvalidQueryOptimizationProposalException(
                                "Internal error: invalid binding transfer application");
                    }
                }
            }
        }
    }


    /**
     * TODO: explain
     */
    private void applyConstructionNodeUpdate(ConstructionNodeUpdate update, IntermediateQuery query, QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException, QueryNodeSubstitutionException {
        QueryNode formerNode = update.getFormerNode();

        Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalSubstitution =
                update.getOptionalSubstitutionToPropagate();

        /**
         * Propagates the substitution to the sub-tree
         */
        if (optionalSubstitution.isPresent()) {
            // SIDE-EFFECT on the tree component (node replacements)
            propagateSubstitutionToSubTree(optionalSubstitution.get(), formerNode, query, treeComponent);
        }

        /**
         * Replaces the node
         */
        ConstructionNode mostRecentNode = update.getMostRecentConstructionNode();
        if (!mostRecentNode.equals(formerNode)) {
            if (stillNeeded(formerNode, mostRecentNode, treeComponent)) {
                treeComponent.replaceNode(formerNode, mostRecentNode);
            }
            else {
                treeComponent.removeOrReplaceNodeByUniqueChildren(formerNode);
            }

        }
    }

    /**
     * TODO: explain
     *
     * SIDE-EFFECT on the treeComponent
     *
     */
    private static void propagateSubstitutionToSubTree(ImmutableSubstitution<VariableOrGroundTerm> substitutionToPropagate,
                                                       QueryNode rootNode, IntermediateQuery query, QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException, QueryNodeSubstitutionException {

        Queue<QueryNode> descendantNodeToTransform = new LinkedList<>();
        // Starts with the children of the root
        descendantNodeToTransform.addAll(treeComponent.getCurrentSubNodesOf(rootNode));

        while (!descendantNodeToTransform.isEmpty()) {
            QueryNode descendantNode = descendantNodeToTransform.poll();

            /**
             * This propagation is stopped by construction nodes
             * (i.e. not applied to them and their descendant)
             */
            if (descendantNode instanceof ConstructionNode) {
                continue;
            }

            // May throw an exception
            SubstitutionResults<? extends QueryNode> substitutionResults =
                    descendantNode.applyDescendingSubstitution(substitutionToPropagate, query);

            Optional<? extends QueryNode> optionalNewDescendantNode = substitutionResults.getOptionalNewNode();
            Optional<? extends ImmutableSubstitution<? extends ImmutableTerm>> optionalNewSubstitution
                    = substitutionResults.getSubstitutionToPropagate();

            /**
             * TODO: handle these cases
             */
            if (!optionalNewSubstitution.isPresent()
                    || substitutionToPropagate.equals(optionalNewSubstitution.get())) {
                throw new RuntimeException("Stopping the propagation or changing the substitution " +
                        "is not supported yet. TO BE DONE");
            }

            /**
             * Standard case:
             *   - adds its children to the queue
             *   - replaces the node if a transformed one has been returned
             */
            if (optionalNewDescendantNode.isPresent()) {
                QueryNode newDescendantNode = optionalNewDescendantNode.get();
                descendantNodeToTransform.addAll(treeComponent.getCurrentSubNodesOf(descendantNode));

                if (!newDescendantNode.equals(descendantNode)) {
                    treeComponent.replaceNode(descendantNode, newDescendantNode);
                }
            }
            /**
             * Current node is not needed anymore
             *
             * TODO: handle this case
             */
            else {
                throw new RuntimeException("TODO: handle this case (substitution propagated after lifting" +
                        "some bindings)");
            }
        }
    }


    /**
     * TODO: explain
     *
     * TODO: externalize
     */
    @Deprecated
    private boolean stillNeeded(QueryNode formerNode, ConstructionNode newNode, QueryTreeComponent treeComponent) {
        if (newNode.getSubstitution().isEmpty() && (!newNode.getOptionalModifiers().isPresent())) {

            /**
             * Checks the parent
             */
            try {
                Optional<QueryNode> optionalParent = treeComponent.getParent(formerNode);
                if (optionalParent.isPresent()) {
                    QueryNode parentNode = optionalParent.get();
                    if (parentNode instanceof UnionNode) {
                        return true;
                    }
                }
                else {
                    return true;
                }
            } catch (IllegalTreeException e) {
                throw new RuntimeException("Internal error: " + e.getMessage());
            }

            ImmutableList<QueryNode> children = treeComponent.getCurrentSubNodesOf(formerNode);
            /**
             * Checks if if still needed by at least one of its children.
             */
            for (QueryNode child : children) {
                if (child instanceof GroupNode)
                    return true;
            }

            return false;
        }
        return true;
    }
}
