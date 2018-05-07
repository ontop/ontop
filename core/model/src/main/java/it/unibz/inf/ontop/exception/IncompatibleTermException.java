package it.unibz.inf.ontop.exception;

import it.unibz.inf.ontop.model.term.Expression;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.type.TermType;

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

    public IncompatibleTermException(String exception, TermType actualTermType) {
        super("Incompatible type inferred " + ": expected: " + exception
                + ", actual: " + actualTermType);
    }

    /**
     * Incompatibility detected in an expression
     */
    public IncompatibleTermException(Expression expression, IncompatibleTermException caughtException) {
        super("In " + expression + ": " + caughtException.getMessage());
    }

    /**
     * Incompatibility detected in an expression
     */
    public IncompatibleTermException(ImmutableExpression expression, IncompatibleTermException caughtException) {
        super("In " + expression + ": " + caughtException.getMessage());
    }

    protected IncompatibleTermException(String message) {
        super(message);
    }
}
