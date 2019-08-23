package it.unibz.inf.ontop.iq.exception;

import it.unibz.inf.ontop.exception.OntopInternalBugException;

/**
 *
 * Invalid OptimizationProposal detected, normally made by IntermediateQueryOptimizer.
 *
 * Internal bug, should not be expected but fixed.
 */
public class InvalidQueryOptimizationProposalException extends OntopInternalBugException {
    public InvalidQueryOptimizationProposalException(String message) {
        super(message);
    }
}
