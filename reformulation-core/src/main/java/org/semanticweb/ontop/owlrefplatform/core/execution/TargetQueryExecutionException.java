package org.semanticweb.ontop.owlrefplatform.core.execution;

/**
 * Exception while executing the target query.
 */
public class TargetQueryExecutionException extends Exception {
    public TargetQueryExecutionException(String message) {
        super(message);
    }
}
