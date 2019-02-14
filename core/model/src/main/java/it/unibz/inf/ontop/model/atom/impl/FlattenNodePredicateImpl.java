package it.unibz.inf.ontop.model.atom.impl;

import it.unibz.inf.ontop.dbschema.FlattenNodeRelationDefinition;
import it.unibz.inf.ontop.model.atom.FlattenNodePredicate;

import static it.unibz.inf.ontop.dbschema.RelationPredicateImpl.extractPredicateName;
import static it.unibz.inf.ontop.dbschema.RelationPredicateImpl.extractTypes;

public class FlattenNodePredicateImpl extends AtomPredicateImpl implements FlattenNodePredicate {

    private final FlattenNodeRelationDefinition relation;

    public FlattenNodePredicateImpl(FlattenNodeRelationDefinition relation) {
        super(extractPredicateName(relation), extractTypes(relation));
        this.relation = relation;
    }

    @Override
    public FlattenNodeRelationDefinition getRelationDefinition() {
        return relation;
    }
}
