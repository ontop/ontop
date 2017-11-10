package it.unibz.inf.ontop.iq.validation;

import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;

/**
 * Partially validates an intermediate query
 * according to 1 or multiple constraints (but not all)
 */
public interface IntermediateQueryValidator {

    void validate(IntermediateQuery query) throws InvalidIntermediateQueryException;

}
