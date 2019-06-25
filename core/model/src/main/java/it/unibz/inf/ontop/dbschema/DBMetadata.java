package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.model.atom.RelationPredicate;

import java.io.Serializable;
import java.util.Collection;

/**
 * Common abstraction for all sorts of Database (relational, etc.)
 */
public interface DBMetadata extends Serializable {

    String getDbmsProductName();

    String getDriverName();

    String getDriverVersion();

    String printKeys();

    /***
     * Generates a map for each predicate in the body of the rules in 'program'
     * that contains the Primary Key data for the predicates obtained from the
     * info in the metadata.
     *
     * It also returns the columns with unique constraints
     *
     * For instance, Given the table definition
     *   Tab0[col1:pk, col2:pk, col3, col4:unique, col5:unique],
     *
     * The methods will return the following Multimap:
     *  { Tab0 -> { [col1, col2], [col4], [col5] } }
     *
     *
     */
    ImmutableMultimap<RelationPredicate,ImmutableList<Integer>> getUniqueConstraints();


    /**
     * Use DBParameters.getQuotedIDFactory() instead
     */
    @Deprecated
    QuotedIDFactory getQuotedIDFactory();

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
     * Retrieves the data definition object based on its name. The
     * <name>name</name> can be either a table name or a view name.
     * If <name>id</name> has schema and the fully qualified id
     * cannot be resolved the the table-only id is used
     *
     * @param name
     */
    RelationDefinition getRelation(RelationID name);

    /**
     * Retrieves the tables list form the metadata.
     */
    Collection<DatabaseRelationDefinition> getDatabaseRelations();

    /**
     * After calling this method, the DBMetadata cannot be modified
     */
    void freeze();

    /**
     * Temporary solution to enable DBMetadata merging
     *
     */
    @Deprecated
    ImmutableMap<RelationID, DatabaseRelationDefinition> copyTables();

    /**
     * Temporary solution to enable DBMetadata merging
     *
     */
    @Deprecated
    ImmutableMap<RelationID, RelationDefinition> copyRelations();

    /**
     * New-gen interface
     *
     * TODO: stop using the DBMetadata object in most of the code but DBParameters instead when needed
     */
    DBParameters getDBParameters();
}
