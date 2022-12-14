package it.unibz.inf.ontop.exception;

public class SparqlRuleException extends OBDASpecificationException {

    public SparqlRuleException(String message) {
        super(message);
    }

    public SparqlRuleException(Exception e) {
        super(e);
    }

    public SparqlRuleException(String prefix, Exception e) {
        super(prefix, e);
    }
}
