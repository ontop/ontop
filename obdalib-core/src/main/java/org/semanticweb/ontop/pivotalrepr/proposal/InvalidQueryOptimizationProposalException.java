package org.semanticweb.ontop.pivotalrepr.proposal;

import org.semanticweb.ontop.exception.OntopLowLevelException;

/**
 *
 * Invalid OptimizationProposal detected, normally made by IntermediateQueryOptimizer.
 *
 * Internal bug, should not be expected but fixed.
 */
public class InvalidQueryOptimizationProposalException extends OntopLowLevelException {
    public InvalidQueryOptimizationProposalException(String message) {
        super(message);
    }
}
