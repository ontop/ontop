package it.unibz.inf.ontop.model.type;

import it.unibz.inf.ontop.model.term.Expression;
import it.unibz.inf.ontop.model.term.Term;

/**
 *
 * Incompatibility found between an expected term type and the one inferred.
 */
public class IncompatibleTermException extends RuntimeException {
    public IncompatibleTermException(Term term, TermType expectedTermType, TermType actualTermType) {
        super("Incompatible type inferred for " + term + ": expected: " + expectedTermType
                + ", actual: " + actualTermType);
    }

    public IncompatibleTermException(TermType expectedTermType, TermType actualTermType) {
        super("Incompatible type inferred " + ": expected: " + expectedTermType
                + ", actual: " + actualTermType);
    }

    /**
     * Incompatibility detected in an expression
     */
    public IncompatibleTermException(Expression expression, IncompatibleTermException caughtException) {
        super("In " + expression + ": " + caughtException.getMessage());
    }
}
