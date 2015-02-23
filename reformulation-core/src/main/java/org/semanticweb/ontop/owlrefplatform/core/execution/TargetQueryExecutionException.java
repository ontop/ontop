package org.semanticweb.ontop.owlrefplatform.core.execution;

import org.semanticweb.ontop.model.OBDAException;

/**
 * Exception while executing the target query.
 */
public class TargetQueryExecutionException extends OBDAException {
    public TargetQueryExecutionException(String message) {
        super(message);
    }
}
