package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

import java.sql.SQLException;

public interface RDBMetadataLoader extends MetadataProvider {

    ImmutableList<RelationID> getRelationIDs() throws MetadataExtractionException;

    RelationID getRelationCanonicalID(RelationID id);

    /**
     * relationID can be mapped to many tables (if, for example, it has no schema)
     *
     * @param relationID
     * @return
     */
    ImmutableList<RelationDefinition.AttributeListBuilder> getRelationAttributes(RelationID relationID) throws MetadataExtractionException;

    void insertIntegrityConstraints(RelationDefinition relation, DBMetadata dbMetadata) throws MetadataExtractionException;
}
