package it.unibz.inf.ontop.dbschema;

import java.util.Collection;

/**
 * Common abstraction for all sorts of Database (relational, etc.)
 */
public interface DBMetadata {

    /**
     * Retrieves the data definition object based on its name. The
     * <name>id</name> is a table name.
     * If <name>id</name> has schema and the fully qualified id
     * cannot be resolved the the table-only id is used
     *
     * @param id
     */
    DatabaseRelationDefinition getDatabaseRelation(RelationID id);

    /**
     * Retrieves the tables list form the metadata.
     */
    Collection<DatabaseRelationDefinition> getDatabaseRelations();

    /**
     * After calling this method, the DBMetadata cannot be modified
     */
    void freeze();

    /**
     * New-gen interface
     *
     * TODO: stop using the DBMetadata object in most of the code but DBParameters instead when needed
     */
    DBParameters getDBParameters();
}
