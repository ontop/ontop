package it.unibz.inf.ontop.exception;

public class MissingTemporalIntermediateQueryNodeException extends Exception {

    public MissingTemporalIntermediateQueryNodeException() {
        super();
    }

    public MissingTemporalIntermediateQueryNodeException(String message) {
        super(message);
    }

    public MissingTemporalIntermediateQueryNodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingTemporalIntermediateQueryNodeException(Throwable cause) {
        super(cause);
    }
}
