package it.unibz.inf.ontop.owlrefplatform.core.execution;

import it.unibz.inf.ontop.model.OBDAException;

/**
 * Exception while executing the native query.
 */
public class NativeQueryExecutionException extends OBDAException {
    public NativeQueryExecutionException(String message) {
        super(message);
    }
}
