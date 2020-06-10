package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.ForeignKeyConstraint;
import it.unibz.inf.ontop.dbschema.FunctionalDependency;
import it.unibz.inf.ontop.dbschema.UniqueConstraint;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;

public class OntopViewDefinition extends AbstractRelationDefinition {

    protected OntopViewDefinition(String predicateName, AttributeListBuilder builder) {
        super(predicateName, builder);
    }

    IQ getDefinition(){
        return null;
    }

    @Override
    public ImmutableList<UniqueConstraint> getUniqueConstraints() {
        return null;
    }

    @Override
    public ImmutableList<FunctionalDependency> getOtherFunctionalDependencies() {
        return null;
    }

    @Override
    public ImmutableList<ForeignKeyConstraint> getForeignKeys() {
        return null;
    }
}
