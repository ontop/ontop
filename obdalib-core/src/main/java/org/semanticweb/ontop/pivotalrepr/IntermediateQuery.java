package org.semanticweb.ontop.pivotalrepr;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.ontop.model.Variable;
import org.semanticweb.ontop.model.impl.VariableImpl;

/**
 *
 */
public interface IntermediateQuery {

    ProjectionNode getRootProjectionNode();

    ImmutableList<QueryNode> getNodesInBottomUpOrder();

    ImmutableList<QueryNode> getCurrentSubNodesOf(QueryNode node);

    boolean contains(QueryNode node);

    /**
     * TODO: describe
     *
     * Returns the QueryNode at the same position, which might be new.
     */
    QueryNode applySubNodeSelectionProposal(NewSubNodeSelectionProposal proposal)
            throws InvalidLocalOptimizationProposalException;

    /**
     * TODO: describe
     *
     * Returns the QueryNode at the same position, which might be new.
     */
    QueryNode applyReplaceNodeProposal(ReplaceNodeProposal proposal)
            throws InvalidLocalOptimizationProposalException;

    /**
     * TODO: describe
     *
     */
    @Deprecated
    QueryNode applyDetypingProposal(DetypingProposal proposal)
            throws InvalidLocalOptimizationProposalException;

    /**
     * TODO: find an exception to throw
     */
    void mergeSubQuery(IntermediateQuery subQuery) throws QueryMergingException;

    /**
     * TODO: explain
     * Does nothing if the node is not "typed".
     */
    @Deprecated
    public void detypeNode(QueryNode nodeToDetype);


    Variable createNewVariable();
}
