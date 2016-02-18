package unibz.inf.ontop.pivotalrepr.proposal;

import unibz.inf.ontop.exception.OntopLowLevelException;

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
