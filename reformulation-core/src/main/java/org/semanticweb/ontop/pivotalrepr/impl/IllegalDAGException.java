package org.semanticweb.ontop.pivotalrepr.impl;

/**
 * Inconsistent query DAG detected.
 *
 * Thrown by low-level operations.
 *
 */
public class IllegalDAGException extends Exception {
    public IllegalDAGException(String message) {
        super(message);
    }
}
