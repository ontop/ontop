package it.unibz.inf.ontop.iq.proposal;

import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.iq.IntermediateQuery;

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
