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

    ImmutableList<QueryNode> getNodesInTopDownOrder();

    ImmutableList<QueryNode> getChildren(QueryNode node);

    /**
     * From the parent to the oldest ancestor.
     */
    ImmutableList<QueryNode> getAncestors(QueryNode descendantNode);

    Optional<QueryNode> getParent(QueryNode node);

    Optional<QueryNode> getNextSibling(QueryNode node);

    Optional<QueryNode> getFirstChild(QueryNode node);

    /**
     * TODO: explain
     */
    Optional<BinaryAsymmetricOperatorNode.ArgumentPosition> getOptionalPosition(QueryNode parentNode, QueryNode child);

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
            throws InvalidQueryOptimizationProposalException, EmptyQueryException;

    /**
     * TODO: find an exception to throw
     */
    void mergeSubQuery(IntermediateQuery subQuery) throws QueryMergingException;

    /**
     *
     * Returns itself if is a ConstructionNode or its first ancestor that is a construction node otherwise.
     */
    ConstructionNode getClosestConstructionNode(QueryNode node);
}
