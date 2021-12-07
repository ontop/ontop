package it.unibz.inf.ontop.dbschema.impl.json;

import it.unibz.inf.ontop.exception.MetadataExtractionException;

public class UnsupportedAddedColumnExpressionException extends MetadataExtractionException {
    public UnsupportedAddedColumnExpressionException(String message) {
        super(message);
    }

    public UnsupportedAddedColumnExpressionException(String message, Exception e) {
        super(message, e);
    }

}
