package it.unibz.inf.ontop.dbschema;

import it.unibz.inf.ontop.exception.MetadataExtractionException;

public interface RDBMetadataProvider extends MetadataProvider {

    RelationID getRelationCanonicalID(RelationID id);

    void insertIntegrityConstraints(RelationDefinition relation, MetadataLookup metadataLookup) throws MetadataExtractionException;

    DBParameters getDBParameters();
}
