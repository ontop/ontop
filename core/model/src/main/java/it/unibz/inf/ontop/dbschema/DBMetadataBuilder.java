package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;

/**
 * Common abstraction for all sorts of Database (relational, etc.)
 */
public interface DBMetadataBuilder {

    DatabaseRelationDefinition createDatabaseRelation(RelationDefinition.AttributeListBuilder builder);

    /**
     * Retrieves the data definition object based on its name.
     *
     * @param id
     */
    DatabaseRelationDefinition getDatabaseRelation(RelationID id);

    DBParameters getDBParameters();

    ImmutableDBMetadata build();
}
