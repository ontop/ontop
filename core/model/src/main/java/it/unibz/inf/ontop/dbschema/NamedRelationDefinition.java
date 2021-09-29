package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;

/**
 * Represents either a database relation (either a table or a view) or an Ontop view
 *
 * @author Roman Kontchakov
 *
 */

public interface NamedRelationDefinition extends RelationDefinition {

	RelationID getID();

	ImmutableSet<RelationID> getAllIDs();

	Optional<UniqueConstraint> getPrimaryKey();


	void addFunctionalDependency(FunctionalDependency constraint);

	void addForeignKeyConstraint(ForeignKeyConstraint fk);
}
