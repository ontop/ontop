package org.semanticweb.ontop.pivotalrepr;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.pivotalrepr.proposal.*;

/**
 *
 */
public interface IntermediateQuery {

    ConstructionNode getRootConstructionNode();

    ImmutableList<QueryNode> getNodesInBottomUpOrder();

    ImmutableList<QueryNode> getCurrentSubNodesOf(QueryNode node);

    /**
     * EXCLUDES the root of the sub-tree (currentNode).
     * TODO: find a better name
     */
    ImmutableList<QueryNode> getSubTreeNodesInTopDownOrder(QueryNode currentNode);

    boolean contains(QueryNode node);

    /**
     * Central method for submitting a proposal.
     * Throws a InvalidQueryOptimizationProposalException if the proposal is rejected.
     *
     * Returns an IntermediateQuery that MIGHT (i) be the current intermediate query that would have been optimized
     * or (ii) a new IntermediateQuery.
     *
     *
     * The proposal is expected TO optimize the query WITHOUT CHANGING ITS SEMANTICS.
     * In principle, the proposal could be carefully checked, beware!
     *
     */
    ProposalResults applyProposal(QueryOptimizationProposal proposal)
        throws InvalidQueryOptimizationProposalException;

    /**
     * TODO: find an exception to throw
     */
    void mergeSubQuery(IntermediateQuery subQuery) throws QueryMergingException;

    /**
     * TODO: explain
     */
    Optional<BinaryAsymmetricOperatorNode.ArgumentPosition> getOptionalPosition(QueryNode parentNode, QueryNode child);

    ImmutableList<QueryNode> getAncestors(QueryNode descendantNode);

    Optional<QueryNode> getParent(QueryNode node);
    
}
