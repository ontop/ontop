package it.unibz.inf.ontop.pivotalrepr.proposal;

import it.unibz.inf.ontop.pivotalrepr.IntensionalDataNode;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;

import java.util.Optional;

/**
 * Merges a sub-query into the IntermediateQuery.
 *
 * Useful for converting a DatalogProgram consisting of multiple rules
 * into an IntermediateQuery.
 *
 * If the subQuery is irrelevant (cannot be merged in), it is simply ignored.
 *
 */
public interface QueryMergingProposal extends QueryOptimizationProposal<ProposalResults> {

    /**
     * Node that will be replaced by the sub-query
     */
    IntensionalDataNode getIntensionalNode();

    /**
     * Sub-query to be merged within the query
     */
    Optional<IntermediateQuery> getSubQuery();
}
