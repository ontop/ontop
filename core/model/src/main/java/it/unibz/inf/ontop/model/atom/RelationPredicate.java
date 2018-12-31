package it.unibz.inf.ontop.model.atom;

import it.unibz.inf.ontop.dbschema.RelationDefinition;


public interface RelationPredicate extends AtomPredicate {

    RelationDefinition getRelationDefinition();
}
