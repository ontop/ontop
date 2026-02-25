package it.unibz.inf.ontop.exception;

import it.unibz.inf.ontop.dbschema.RelationID;

import java.util.Collection;

public class RelationNotFoundInMetadataException extends MetadataExtractionException {
    public RelationNotFoundInMetadataException(RelationID id, Collection<RelationID> choices) {
        this(id, choices, "");
    }
    public RelationNotFoundInMetadataException(RelationID id, Collection<RelationID> choices, String additionalMessage) {
        super("Cannot find relation " + id + " (available choices: " + choices + ")" + additionalMessage);
    }
}
