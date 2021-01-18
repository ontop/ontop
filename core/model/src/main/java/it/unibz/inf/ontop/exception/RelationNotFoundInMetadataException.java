package it.unibz.inf.ontop.exception;

import it.unibz.inf.ontop.dbschema.RelationID;

import java.util.Collection;

public class RelationNotFoundInMetadataException extends MetadataExtractionException {
    public RelationNotFoundInMetadataException(RelationID id, Collection<RelationID> choices) {
        super("Cannot find relation " + id + " (available choices: " + choices + ")");
    }
}
