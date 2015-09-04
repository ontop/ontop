package org.semanticweb.ontop.pivotalrepr.impl;

/**
 * Inconsistent query tree detected.
 *
 * Thrown by low-level operations.
 * Usually does not need to be expected.
 *
 */
public class IllegalTreeException extends RuntimeException {
    public IllegalTreeException(String message) {
        super(message);
    }
}
