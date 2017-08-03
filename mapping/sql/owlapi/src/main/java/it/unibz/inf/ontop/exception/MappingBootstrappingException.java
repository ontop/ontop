package it.unibz.inf.ontop.exception;

/**
 * Problem occurred while boostrapping the mapping
 */
public class MappingBootstrappingException extends Exception {

    public MappingBootstrappingException(Exception e) {
        super(e);
    }
}
