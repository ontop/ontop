package it.unibz.inf.ontop.spec.sqlparser.exception;

/**
 * Created by Roman Kontchakov on 28/01/2017.
 *
 * An internal run-time exception class to work with the JSQLParser
 * interfaces. DO NOT USE ELSEWHERE.
 *
 */
public class UnsupportedSelectQueryRuntimeException extends RuntimeException {
    private final Object object;

    public UnsupportedSelectQueryRuntimeException(String message, Object object) {
        super(message);
        this.object = object;
    }

    public Object getObject() {
        return object;
    }
}



