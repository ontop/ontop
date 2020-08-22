package it.unibz.inf.ontop.iq.exception;

/**
 * After optimization, the query becomes empty
 *  {@code ---> } will return no result.
 */
public class EmptyQueryException extends Exception {
    public EmptyQueryException() {
        super("Empty query");
    }
}
