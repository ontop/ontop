package it.unibz.inf.ontop.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.sql.DatabaseRelationDefinition;
import it.unibz.inf.ontop.sql.QuotedIDFactory;
import it.unibz.inf.ontop.sql.RelationID;

import java.io.Serializable;
import java.util.Collection;

/**
 * Common abstraction for all sorts of Database (relational, etc.)
 */
public interface DataSourceMetadata extends Serializable {

    String getDriverName();

    String getDriverVersion();

    String printKeys();

    ImmutableMultimap<AtomPredicate,ImmutableList<Integer>> extractUniqueConstraints();

    ImmutableMultimap<AtomPredicate, CQIE> generateFKRules();

    QuotedIDFactory getQuotedIDFactory();

    DatabaseRelationDefinition getDatabaseRelation(RelationID id);

    Collection<DatabaseRelationDefinition> getDatabaseRelations();


}
