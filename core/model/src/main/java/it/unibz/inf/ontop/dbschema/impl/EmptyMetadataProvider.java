package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;


public class EmptyMetadataProvider implements MetadataProvider {

    @Override
    public ImmutableList<RelationID> getRelationIDs() {
        return ImmutableList.of();
    }

    @Override
    public RelationDefinition getRelation(RelationID relationId) throws MetadataExtractionException {
        throw new MetadataExtractionException("Relation " + relationId + " not found");
    }

    @Override
    public void insertIntegrityConstraints(ImmutableDBMetadata md) {
    }
}
