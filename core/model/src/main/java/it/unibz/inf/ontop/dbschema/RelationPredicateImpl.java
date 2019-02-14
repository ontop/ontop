package it.unibz.inf.ontop.dbschema;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.atom.impl.AtomPredicateImpl;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;


public class  RelationPredicateImpl extends AtomPredicateImpl implements RelationPredicate {

    protected final RelationDefinition relation;

    protected RelationPredicateImpl(RelationDefinition relation) {
        super(extractPredicateName(relation), extractTypes(relation));
        this.relation = relation;
    }

    public static  String extractPredicateName(RelationDefinition r) {
        RelationID id = r.getID();
        String name = id.getSchemaName();
        if (name == null)
            name =  id.getTableName();
        else
            name = name + "." + id.getTableName();
        return name;
    }

    public static ImmutableList<TermType> extractTypes(RelationDefinition relation) {
        return relation.getAttributes().stream()
                .map(Attribute::getTermType)
                .collect(ImmutableCollectors.toList());
    }

    @Override
    public RelationDefinition getRelationDefinition() {
        return relation;
    }
}
