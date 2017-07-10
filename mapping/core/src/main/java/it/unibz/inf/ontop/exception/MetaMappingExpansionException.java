package it.unibz.inf.ontop.exception;


public class MetaMappingExpansionException extends MappingException {

    public MetaMappingExpansionException(String message) {
        super(message);
    }

    public MetaMappingExpansionException(Exception e) {
        super(e);
    }
}
