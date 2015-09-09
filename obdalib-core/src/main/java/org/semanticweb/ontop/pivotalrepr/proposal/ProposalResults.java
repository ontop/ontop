package org.semanticweb.ontop.pivotalrepr.proposal;

import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;

/**
 * Results returned after the application of a QueryOptimizationProposal.
 *
 */
public interface ProposalResults {

    IntermediateQuery getResultingQuery();
}
