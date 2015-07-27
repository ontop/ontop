package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.VariableOrGroundTerm;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.proposal.BindingTransfer;
import org.semanticweb.ontop.pivotalrepr.proposal.ConstructionNodeUpdate;
import org.semanticweb.ontop.pivotalrepr.proposal.InvalidQueryOptimizationProposalException;
import org.semanticweb.ontop.pivotalrepr.proposal.SubstitutionLiftProposal;

/**
 * TODO: explain
 */
public class SubstitutionLiftProposalExecutor implements InternalProposalExecutor<SubstitutionLiftProposal> {

    @Override
    public void apply(SubstitutionLiftProposal proposal, QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException {
        for (BindingTransfer bindingTransfer : proposal.getBindingTransfers()) {
            applyBindingTransfer(bindingTransfer, treeComponent);
        }

        for (ConstructionNodeUpdate update : proposal.getNodeUpdates()) {
            applyConstructionNodeUpdate(update, treeComponent);
        }
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
            BindingTransferTransformer transformer = new BindingTransferTransformer(bindingTransfer);
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
                        throw new RuntimeException("Internal error: invalid binding transfer application");
                    }
                }
            }
        }
    }


    /**
     * TODO: explain
     */
    private void applyConstructionNodeUpdate(ConstructionNodeUpdate update, QueryTreeComponent treeComponent)
            throws InvalidQueryOptimizationProposalException {
        QueryNode formerNode = update.getFormerNode();

        Optional<ImmutableSubstitution<VariableOrGroundTerm>> optionalSubstitution =
                update.getOptionalSubstitutionToPropagate();

        /**
         * Propagates the substitution to the sub-tree
         */
        if (optionalSubstitution.isPresent()) {

            SubstitutionPropagator propagator = new SubstitutionPropagator(optionalSubstitution.get());
            for (QueryNode descendantNode : treeComponent.getSubTreeNodesInTopDownOrder(formerNode)) {
                try {
                    QueryNode newDescendantNode = descendantNode.acceptNodeTransformer(propagator);
                    if (!newDescendantNode.equals(descendantNode)) {
                        treeComponent.replaceNode(descendantNode, newDescendantNode);
                    }
                } catch (QueryNodeTransformationException e) {
                    throw new InvalidQueryOptimizationProposalException(e.getMessage());
                } catch (NotNeededNodeException e) {
                    throw new RuntimeException("TODO: handle this case (substitution propagated after lifting" +
                            "some bindings)");
                }
            }
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
                try {
                    treeComponent.removeOrReplaceNodeByUniqueChildren(formerNode);
                } catch (IllegalTreeUpdateException e) {
                    throw new RuntimeException("Internal error: " + e.getMessage());
                }
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
