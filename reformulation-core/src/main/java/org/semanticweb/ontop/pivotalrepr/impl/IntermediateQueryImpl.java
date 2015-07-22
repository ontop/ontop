package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.AtomPredicate;
import org.semanticweb.ontop.model.DataAtom;
import org.semanticweb.ontop.model.ImmutableSubstitution;
import org.semanticweb.ontop.model.VariableOrGroundTerm;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.pivotalrepr.*;
import org.semanticweb.ontop.pivotalrepr.proposal.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * TODO: describe
 *
 * BEWARE: this class has a non-trivial mutable internal state!
 */
public class IntermediateQueryImpl implements IntermediateQuery {

    /**
     * Thrown when the internal state of the intermediate query is found to be inconsistent.
     *
     * Should not be expected (internal error).
     *
     */
    protected static class InconsistentIntermediateQueryException extends RuntimeException {
        protected InconsistentIntermediateQueryException(String message) {
            super(message);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(IntermediateQueryImpl.class);

    /**
     * TODO: use Guice to replace it.
     */
    private static final IntermediateQueryPrinter PRINTER = new BasicQueryTreePrinter();


    /**
     * Highly mutable (low control) so MUST NOT BE SHARED!
     */
    private final QueryTreeComponent treeComponent;

    /**
     * For IntermediateQueryBuilders ONLY!!
     */
    protected IntermediateQueryImpl(QueryTreeComponent treeComponent) {
        this.treeComponent = treeComponent;
    }

    @Override
    public ConstructionNode getRootConstructionNode() throws InconsistentIntermediateQueryException{
        try {
            return treeComponent.getRootConstructionNode();
        } catch (IllegalTreeException e) {
            throw new InconsistentIntermediateQueryException(e.getMessage());
        }
    }

    @Override
    public ImmutableList<QueryNode> getNodesInBottomUpOrder() throws InconsistentIntermediateQueryException {
        try {
            return treeComponent.getNodesInBottomUpOrder();
        } catch (IllegalTreeException e) {
            throw new InconsistentIntermediateQueryException(e.getMessage());
        }
    }

    @Override
    public ImmutableList<QueryNode> getCurrentSubNodesOf(QueryNode node) {
        return treeComponent.getCurrentSubNodesOf(node);
    }

    @Override
    public ImmutableList<QueryNode> getSubTreeNodesInTopDownOrder(QueryNode currentNode) {
        return treeComponent.getSubTreeNodesInTopDownOrder(currentNode);
    }

    @Override
    public boolean contains(QueryNode node) {
        return treeComponent.contains(node);
    }

    /**
     * The order of sub-node selection is ignored.
     */
    @Override
    public QueryNode applySubNodeSelectionProposal(NewSubNodeSelectionProposal proposal)
            throws InvalidLocalOptimizationProposalException {
        QueryNode currentNode = proposal.getQueryNode();

        try {
            treeComponent.setChildrenNodes(currentNode, proposal.getSubNodes());
            return currentNode;
        } catch(IllegalTreeException e) {
            throw new InvalidLocalOptimizationProposalException(e.getLocalizedMessage());
        }
    }

    @Override
    public QueryNode applyReplaceNodeProposal(ReplaceNodeProposal proposal)
            throws InvalidLocalOptimizationProposalException {
        QueryNode nodeToReplace = proposal.getNodeToReplace();

        if (!contains(nodeToReplace)) {
            throw new InvalidLocalOptimizationProposalException("No such node to replace: " + nodeToReplace);
        }

        // TODO: check more

        QueryNode replacingNode = proposal.getReplacingNode();
        treeComponent.replaceNode(nodeToReplace,replacingNode);

        return replacingNode;
    }

    @Override
    public void applySubstitutionLiftProposal(SubstitutionLiftProposal substitutionLiftProposal)
            throws InvalidLocalOptimizationProposalException {

        for (BindingTransfer bindingTransfer : substitutionLiftProposal.getBindingTransfers()) {
            applyBindingTransfer(bindingTransfer);
        }

        for (ConstructionNodeUpdate update :substitutionLiftProposal.getNodeUpdates()) {
            applyConstructionNodeUpdate(update);
        }
    }

    @Override
    public Optional<BinaryAsymmetricOperatorNode.ArgumentPosition> getOptionalPosition(QueryNode parentNode,
                                                                                      QueryNode childNode) {
        return treeComponent.getOptionalPosition(parentNode, childNode);
    }

    @Override
    public ImmutableList<QueryNode> getAncestors(QueryNode descendantNode) {
        try {
            return treeComponent.getAncestors(descendantNode);
        } catch (IllegalTreeException e) {
            throw new InconsistentIntermediateQueryException(e.getMessage());
        }
    }

    /**
     * TODO: explain
     */
    @Override
    public IntermediateQuery newWithDifferentConstructionPredicate(AtomPredicate formerPredicate, AtomPredicate newPredicate)
            throws AlreadyExistingPredicateException {

        PredicateRenamingChecker.checkNonExistence(this, newPredicate);
        PredicateRenamer renamer = new PredicateRenamer(formerPredicate, newPredicate);
        try {
            return IntermediateQueryUtils.convertToBuilderAndTransform(this, renamer).build();
        }
        catch (IntermediateQueryBuilderException | QueryNodeTransformationException | NotNeededNodeException e) {
            throw new RuntimeException("Unexpected error: " + e.getMessage());
        }
    }

    /**
     * TODO: explain
     */
    @Override
    public void mergeSubQuery(final IntermediateQuery originalSubQuery) throws QueryMergingException {
        /**
         * TODO: explain
         */
        List<OrdinaryDataNode> localDataNodes = findOrdinaryDataNodes(originalSubQuery.getRootConstructionNode().getProjectionAtom());
        if (localDataNodes.isEmpty())
            throw new QueryMergingException("No OrdinaryDataNode matches " + originalSubQuery.getRootConstructionNode().getProjectionAtom());


        for (OrdinaryDataNode localDataNode : localDataNodes) {
            // TODO: make it be incremental
            ImmutableSet<VariableImpl> localVariables = VariableCollector.collectVariables(this);

            try {
                IntermediateQuery cloneSubQuery = SubQueryUnificationTools.unifySubQuery(originalSubQuery,
                            localDataNode.getAtom(), localVariables);

                ConstructionNode subQueryRootNode = cloneSubQuery.getRootConstructionNode();
                treeComponent.replaceNode(localDataNode, subQueryRootNode);

                treeComponent.addSubTree(cloneSubQuery, subQueryRootNode);
            } catch (SubQueryUnificationTools.SubQueryUnificationException e) {
                throw new QueryMergingException(e.getMessage());
            }
        }
    }

    /**
     * Finds ordinary data nodes.
     *
     * TODO: explain
     */
    private ImmutableList<OrdinaryDataNode> findOrdinaryDataNodes(DataAtom subsumingDataAtom)
            throws InconsistentIntermediateQueryException {
        ImmutableList.Builder<OrdinaryDataNode> listBuilder = ImmutableList.builder();
        try {
            for(QueryNode node : treeComponent.getNodesInBottomUpOrder()) {
                if (node instanceof OrdinaryDataNode) {
                    OrdinaryDataNode dataNode = (OrdinaryDataNode) node;
                    if (subsumingDataAtom.hasSamePredicateAndArity(dataNode.getAtom()))
                        listBuilder.add(dataNode);
                }
            }
        } catch (IllegalTreeException e) {
            throw new InconsistentIntermediateQueryException(e.getMessage());
        }
        return listBuilder.build();
    }


    /**
     * Not appearing in the interface because users do not
     * have to worry about it.
     */
    @Override
    public IntermediateQuery clone() {
        try {
            return IntermediateQueryUtils.convertToBuilder(this).build();
        } catch (IntermediateQueryBuilderException e) {
            throw new RuntimeException("BUG (internal error)!" + e.getLocalizedMessage());
        }
    }

    @Override
    public String toString() {
        return PRINTER.stringify(this);
    }

    /**
     * TODO: explain
     */
    private void applyBindingTransfer(BindingTransfer bindingTransfer) throws InvalidLocalOptimizationProposalException {
        ConstructionNode targetNode = bindingTransfer.getTargetNode();

        for (ConstructionNode sourceNode : bindingTransfer.getSourceNodes()) {
            ImmutableList<QueryNode> ancestors;
            try {
                ancestors = treeComponent.getAncestors(sourceNode);
            } catch (IllegalTreeException e) {
                throw new InvalidLocalOptimizationProposalException("The source node " + sourceNode + " is not ");
            }
            if (!ancestors.contains(targetNode)) {
                throw new InvalidLocalOptimizationProposalException("The target node " + targetNode
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
                    throw new InvalidLocalOptimizationProposalException(e.getMessage());
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
    private void applyConstructionNodeUpdate(ConstructionNodeUpdate update) throws InvalidLocalOptimizationProposalException {
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
                    throw new InvalidLocalOptimizationProposalException(e.getMessage());
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
            if (stillNeeded(formerNode, mostRecentNode)) {
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
    private boolean stillNeeded(QueryNode formerNode, ConstructionNode newNode) {
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
