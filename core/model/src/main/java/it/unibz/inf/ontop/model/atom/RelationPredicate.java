package it.unibz.inf.ontop.model.atom;

import it.unibz.inf.ontop.dbschema.RelationDefinition;


public interface RelationPredicate<T extends RelationDefinition> extends AtomPredicate {

    T getRelationDefinition();
}
