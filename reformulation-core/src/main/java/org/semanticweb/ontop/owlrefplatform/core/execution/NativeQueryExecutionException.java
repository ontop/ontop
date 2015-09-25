package org.semanticweb.ontop.owlrefplatform.core.execution;

import org.semanticweb.ontop.model.OBDAException;

/**
 * Exception while executing the native query.
 */
public class NativeQueryExecutionException extends OBDAException {
    public NativeQueryExecutionException(String message) {
        super(message);
    }
}
