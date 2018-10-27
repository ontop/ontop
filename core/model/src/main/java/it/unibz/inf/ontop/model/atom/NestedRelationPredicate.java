package it.unibz.inf.ontop.model.atom;

import it.unibz.inf.ontop.dbschema.NestedDatabaseRelationDefinition;

/**
 * TODO: define a common abstraction with {@link RelationPredicate}
 */
public interface NestedRelationPredicate extends AtomPredicate{

    NestedDatabaseRelationDefinition getRelationDefinition();
}
