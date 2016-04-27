package it.unibz.inf.ontop.model.type;

import it.unibz.inf.ontop.exception.OntopRuntimeException;
import it.unibz.inf.ontop.model.Expression;
import it.unibz.inf.ontop.model.Term;
import it.unibz.inf.ontop.model.TermType;

/**
 * TODO: better integrate into the Ontop exception hierarchy
 *
 * Incompatibility found between an expected term type and the one inferred.
 */
public class TermTypeException extends OntopRuntimeException {
    public TermTypeException(Term term, TermType expectedTermType, TermType actualTermType) {
        super("Incompatible type inferred for " + term + ": expected: " + expectedTermType
                + ", actual: " + actualTermType);
    }

    /**
     * Incompatibility detected in an expression
     */
    public TermTypeException(Expression expression, TermTypeException caughtException) {
        super("In " + expression + ": " + caughtException.getMessage());
    }
}
