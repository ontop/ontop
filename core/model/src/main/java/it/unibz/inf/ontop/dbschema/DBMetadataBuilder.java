package it.unibz.inf.ontop.dbschema;

/**
 * Common abstraction for all sorts of Database (relational, etc.)
 */
public interface DBMetadataBuilder {

    DatabaseRelationDefinition createDatabaseRelation(RelationDefinition.AttributeListBuilder builder);

    DBParameters getDBParameters();

    ImmutableDBMetadata build();
}
