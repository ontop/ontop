package it.unibz.inf.ontop.exception;

import it.unibz.inf.ontop.dbschema.RelationID;

public class RelationNotFoundInMetadataException extends MetadataExtractionException {
    public RelationNotFoundInMetadataException(RelationID id) {
        super("Cannot find relation id: " + id);
    }
}
