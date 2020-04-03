package it.unibz.inf.ontop.dbschema;

import it.unibz.inf.ontop.exception.MetadataExtractionException;

public interface RDBMetadataProvider extends MetadataProvider {

    RelationID getRelationCanonicalID(RelationID id);

    void insertIntegrityConstraints(RelationDefinition relation, DBMetadata dbMetadata) throws MetadataExtractionException;

    DBParameters getDBParameters();
}
