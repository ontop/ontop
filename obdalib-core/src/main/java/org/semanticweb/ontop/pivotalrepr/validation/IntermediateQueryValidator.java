package org.semanticweb.ontop.pivotalrepr.validation;

import org.semanticweb.ontop.pivotalrepr.IntermediateQuery;

/**
 * Partially validates an intermediate query
 * according to 1 or multiple constraints (but not all)
 */
public interface IntermediateQueryValidator {

    void validate(IntermediateQuery query) throws InvalidIntermediateQueryException;

}
