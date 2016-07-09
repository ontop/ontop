package it.unibz.inf.ontop.pivotalrepr.proposal;

import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;

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
     * Sub-query to be merged within the query
     */
    IntermediateQuery getSubQuery();
}
