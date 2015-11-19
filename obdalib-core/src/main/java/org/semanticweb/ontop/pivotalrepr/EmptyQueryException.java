package org.semanticweb.ontop.pivotalrepr;

/**
 * After optimization, the query becomes empty
 *  ---> will return no result.
 */
public class EmptyQueryException extends Exception {
    public EmptyQueryException() {
        super("Empty query");
    }
}
