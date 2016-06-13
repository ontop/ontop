package it.unibz.inf.ontop.pivotalrepr.proposal;

import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;

/**
 * Results returned after the application of a QueryOptimizationProposal.
 *
 */
public interface ProposalResults {

    IntermediateQuery getResultingQuery();
}
