package it.unibz.inf.ontop.pivotalrepr.proposal;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode;
import it.unibz.inf.ontop.pivotalrepr.NonCommutativeOperatorNode.ArgumentPosition;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;

import java.util.Optional;

/**
 * Low-level proposal, to be used by ProposalExecutors only!
 *
 * TODO: explain
 *
 * The child is already deleted.
 *
 * Please note that a cascade of deletion may appear.
 *
 */
public interface ReactToChildDeletionProposal extends QueryOptimizationProposal<ReactToChildDeletionResults> {

    /**
     * Parent of the child that has been removed from the query.
     */
    QueryNode getParentNode();

    Optional<QueryNode> getOptionalNextSibling();

    Optional<ArgumentPosition> getOptionalPositionOfDeletedChild();

    ImmutableSet<Variable> getVariablesProjectedByDeletedChild();
}
