package it.unibz.inf.ontop.owlrefplatform.core.execution;

/**
 * Exception while executing the native query.
 */
public class NativeQueryExecutionException extends RuntimeException {
    public NativeQueryExecutionException(String message) {
        super(message);
    }
}
