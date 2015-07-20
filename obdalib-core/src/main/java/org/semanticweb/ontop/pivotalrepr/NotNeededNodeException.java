package org.semanticweb.ontop.pivotalrepr;

/**
 * When the QueryNode is not needed anymore.
 *
 * SUCH A QUERY NODE MUST HAVE AT MOST ONE CHILD.
 *
 */
public class NotNeededNodeException extends Exception {
    public NotNeededNodeException(String message) {
        super(message);
    }
}
