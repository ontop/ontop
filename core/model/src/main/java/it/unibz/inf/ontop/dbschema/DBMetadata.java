package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.model.type.TermType;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;

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
    ImmutableMultimap<AtomPredicate,ImmutableList<Integer>> getUniqueConstraints();

    /**
     * generate CQIE rules from foreign key info of db metadata
     * TABLE1.COL1 references TABLE2.COL2 as foreign key then
     * construct CQIE rule TABLE2(P1, P3, COL2, P4) :- TABLE1(COL2, T2, T3).
     */
    ImmutableMultimap<AtomPredicate, CQIE> generateFKRules();

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

    Optional<TermType> getTermType(Attribute attribute);

    Optional<DatabaseRelationDefinition> getDatabaseRelationByPredicate(AtomPredicate predicate);

    Relation2Predicate getRelation2Predicate();

    /**
     * After calling this method, the DBMetadata cannot be modified
     */
    void freeze();

}
