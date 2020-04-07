package it.unibz.inf.ontop.dbschema;

import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.atom.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.utils.ImmutableCollectors;


public class RelationPredicateImpl extends AtomPredicateImpl implements RelationPredicate {

    private final RelationDefinition relation;

    protected RelationPredicateImpl(RelationDefinition relation) {
        super(relation.getID().getSQLRendering(),
                relation.getAttributes().stream()
                    .map(Attribute::getTermType)
                    .collect(ImmutableCollectors.toList()));
        this.relation = relation;
    }

    @Override
    public RelationDefinition getRelationDefinition() {
        return relation;
    }
}
