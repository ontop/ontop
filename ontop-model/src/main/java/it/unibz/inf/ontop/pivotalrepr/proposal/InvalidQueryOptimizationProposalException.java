package it.unibz.inf.ontop.pivotalrepr.proposal;

import it.unibz.inf.ontop.exception.OntopIllegalStateException;

/**
 *
 * Invalid OptimizationProposal detected, normally made by IntermediateQueryOptimizer.
 *
 * Internal bug, should not be expected but fixed.
 */
public class InvalidQueryOptimizationProposalException extends OntopIllegalStateException {
    public InvalidQueryOptimizationProposalException(String message) {
        super(message);
    }
}
