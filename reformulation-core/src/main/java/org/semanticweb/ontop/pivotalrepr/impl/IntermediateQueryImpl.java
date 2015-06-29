package org.semanticweb.ontop.pivotalrepr.impl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.DataAtom;
import org.semanticweb.ontop.model.impl.VariableImpl;
import org.semanticweb.ontop.owlrefplatform.core.optimization.DetypingOptimizer;
import org.semanticweb.ontop.pivotalrepr.*;
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

    /**
     * TODO: implement it
     *
     */
    @Override
    public QueryNode applyReplaceNodeProposal(ReplaceNodeProposal proposal)
            throws InvalidLocalOptimizationProposalException {
        throw new RuntimeException("TODO: implement it");
    }

    @Override
    @Deprecated
    public void detypeNode(QueryNode nodeToDetype) {

        if (!contains(nodeToDetype)) {
            throw new IllegalArgumentException("The node is not contained in the query");
        }

        DetypingOptimizer optimizer = new DetypingOptimizer(this);
        Optional<LocalOptimizationProposal> optionalProposal = nodeToDetype.acceptOptimizer(optimizer);

        if (!optionalProposal.isPresent()) {
            LOGGER.debug(nodeToDetype + " was not typed (thus nothing to detype).");
        }
        else {
            try {
                optionalProposal.get().apply();
            }
            /**
             * Should not happen since we created the proposal here
             */
            catch (InvalidLocalOptimizationProposalException e) {
                throw new RuntimeException("Internal error while detyping a node: " + e.getLocalizedMessage());
            }
        }
    }

    @Override
    @Deprecated
    public QueryNode applyDetypingProposal(DetypingProposal proposal)
            throws InvalidLocalOptimizationProposalException {
        throw new RuntimeException("TODO: implement it");
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
    public IntermediateQuery clone() throws CloneNotSupportedException {
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
}
