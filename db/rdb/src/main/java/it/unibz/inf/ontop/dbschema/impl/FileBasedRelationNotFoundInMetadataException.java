package it.unibz.inf.ontop.dbschema.impl;

import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.RelationNotFoundInMetadataException;

import java.util.Collection;

public class FileBasedRelationNotFoundInMetadataException extends RelationNotFoundInMetadataException {
    public FileBasedRelationNotFoundInMetadataException(RelationID id, Collection<RelationID> choices) {
        super(id, choices, "; file-based relation cannot be found either");
    }
}
