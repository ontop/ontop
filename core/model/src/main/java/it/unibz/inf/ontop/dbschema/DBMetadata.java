package it.unibz.inf.ontop.dbschema;

import java.util.Collection;

/**
 * Common abstraction for all sorts of Database (relational, etc.)
 */
public interface DBMetadata extends ImmutableDBMetadata {

    /**
     * Retrieves the data definition object based on its name. The
     * <name>id</name> is a table name.
     * If <name>id</name> has schema and the fully qualified id
     * cannot be resolved the the table-only id is used
     *
     * @param id
     */
    DatabaseRelationDefinition getDatabaseRelation(RelationID id);

}
