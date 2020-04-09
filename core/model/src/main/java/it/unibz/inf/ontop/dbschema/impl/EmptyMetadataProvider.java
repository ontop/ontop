package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.*;

import java.util.Optional;

public class EmptyMetadataProvider implements MetadataProvider {

    @Override
    public ImmutableList<RelationID> getRelationIDs() {
        return ImmutableList.of();
    }

    @Override
    public Optional<RelationDefinition> getRelation(RelationID relationId) { return Optional.empty(); }

    @Override
    public void insertIntegrityConstraints(ImmutableDBMetadata md) {
    }
}
