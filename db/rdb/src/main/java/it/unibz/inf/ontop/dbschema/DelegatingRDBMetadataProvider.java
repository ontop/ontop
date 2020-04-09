package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

import java.util.Optional;

public class DelegatingRDBMetadataProvider implements RDBMetadataProvider {
    protected final RDBMetadataProvider provider;

    public DelegatingRDBMetadataProvider(RDBMetadataProvider provider) {
        this.provider = provider;
    }

    @Override
    public RelationID getRelationCanonicalID(RelationID id) {
        return provider.getRelationCanonicalID(id);
    }

    @Override
    public DBParameters getDBParameters() {
        return provider.getDBParameters();
    }

    @Override
    public ImmutableList<RelationID> getRelationIDs() throws MetadataExtractionException {
        return provider.getRelationIDs();
    }

    @Override
    public Optional<RelationDefinition> getRelation(RelationID relationId) throws MetadataExtractionException {
        return provider.getRelation(relationId);
    }

    @Override
    public void insertIntegrityConstraints(ImmutableDBMetadata md) throws MetadataExtractionException {
        provider.insertIntegrityConstraints(md);
    }
}
