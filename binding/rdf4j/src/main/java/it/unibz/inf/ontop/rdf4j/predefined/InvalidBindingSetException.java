package it.unibz.inf.ontop.rdf4j.predefined;

public class InvalidBindingSetException extends Exception {

    public InvalidBindingSetException(String message) {
        super(message);
    }

    public InvalidBindingSetException(Exception e) {
        super(e);
    }

    public InvalidBindingSetException(String message, Exception e) {
        super(message, e);
    }
}
