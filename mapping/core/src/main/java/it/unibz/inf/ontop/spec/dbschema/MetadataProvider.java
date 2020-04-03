package it.unibz.inf.ontop.spec.dbschema;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.DBMetadataExtractionException;

import java.sql.SQLException;
import java.util.Set;

/**
 */
public interface MetadataProvider {

    /**
     * Extracts relation IDs for all relations
     * @return relation IDs
     */
    ImmutableList<RelationID> getRelationIDs() throws DBMetadataExtractionException;

    /**
     * relationID can be mapped to many tables (if, for example, it has no schema)
     *
     * @param relationID
     * @return
     */
    ImmutableList<RelationDefinition.AttributeListBuilder> getRelationAttributes(RelationID relationID) throws DBMetadataExtractionException;

    /**
     * Inserts the user-supplied primary keys, unique constraints and foreign keys
     * into the metadata object
     */
    void insertIntegrityConstraints(DBMetadata md) throws DBMetadataExtractionException;
}
