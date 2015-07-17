package org.semanticweb.ontop.pivotalrepr;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.AtomPredicate;
import org.semanticweb.ontop.pivotalrepr.proposal.InvalidLocalOptimizationProposalException;
import org.semanticweb.ontop.pivotalrepr.proposal.NewSubNodeSelectionProposal;
import org.semanticweb.ontop.pivotalrepr.proposal.ReplaceNodeProposal;
import org.semanticweb.ontop.pivotalrepr.proposal.SubstitutionLiftProposal;

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
     * TODO: describe
     *
     * Returns the QueryNode at the same position, which might be new.
     */
    @Deprecated
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
     * TODO: describe
     *
     */
    void applySubstitutionLiftProposal(SubstitutionLiftProposal substitutionLiftProposal)
            throws InvalidLocalOptimizationProposalException;

    /**
     * TODO: find an exception to throw
     */
    void mergeSubQuery(IntermediateQuery subQuery) throws QueryMergingException;

    /**
     * TODO: explain
     */
    Optional<BinaryAsymmetricOperatorNode.ArgumentPosition> getOptionalPosition(QueryNode parentNode, QueryNode child);

    ImmutableList<QueryNode> getAncestors(QueryNode descendantNode);

    /**
     * Returns a new IntermediateQuery using the new predicate instead of the former one in some construction nodes.
     */
    IntermediateQuery newWithDifferentConstructionPredicate(AtomPredicate formerPredicate, AtomPredicate newPredicate)
            throws AlreadyExistingPredicateException;
}
