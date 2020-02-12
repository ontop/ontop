package it.unibz.inf.ontop.exception;

import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Term;
import it.unibz.inf.ontop.model.type.TermType;

/**
 * FATAL typing error. NOT relevant for SPARQL errors (which are not fatal).
 *
 * Appears due:
 *    - A problem in the mapping (e.g. the source query is generating a SQL error)
 *    - A bug inside Ontop (when the mapping is valid, this error should never occur)
 *
 * TODO: integrate in the Ontop exception hierarchy
 *
 * TODO: refactor the error messages
 */
public class FatalTypingException extends Exception {
    public FatalTypingException(Term term, TermType expectedTermType, TermType actualTermType) {
        super("Incompatible type inferred for " + term + ": expected: " + expectedTermType
                + ", actual: " + actualTermType);
    }

    public FatalTypingException(TermType expectedTermType, TermType actualTermType) {
        super("Incompatible type inferred " + ": expected: " + expectedTermType
                + ", actual: " + actualTermType);
    }

    public FatalTypingException(String exception, TermType actualTermType) {
        super("Incompatible type inferred " + ": expected: " + exception
                + ", actual: " + actualTermType);
    }

    /**
     * Incompatibility detected in an expression
     */
    public FatalTypingException(ImmutableExpression expression, FatalTypingException caughtException) {
        super("In " + expression + ": " + caughtException.getMessage());
    }

    protected FatalTypingException(String message) {
        super(message);
    }
}
