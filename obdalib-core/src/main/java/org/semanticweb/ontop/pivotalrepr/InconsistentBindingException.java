package org.semanticweb.ontop.pivotalrepr;

/**
 * When "updating" a ConstructionNode with bindings to add or to remove.
 */
public class InconsistentBindingException extends RuntimeException {
    public InconsistentBindingException(String message) {
        super(message);
    }
}
