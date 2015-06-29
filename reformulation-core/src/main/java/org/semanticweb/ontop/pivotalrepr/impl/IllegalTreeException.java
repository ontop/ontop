package org.semanticweb.ontop.pivotalrepr.impl;

/**
 * Inconsistent query tree detected.
 *
 * Thrown by low-level operations.
 *
 */
public class IllegalTreeException extends Exception {
    public IllegalTreeException(String message) {
        super(message);
    }
}
