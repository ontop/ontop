package it.unibz.inf.ontop.iq.node.impl;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;

public abstract class CompositeQueryNodeImpl extends QueryNodeImpl {

    final SubstitutionFactory substitutionFactory;
    final IntermediateQueryFactory iqFactory;

    protected CompositeQueryNodeImpl(SubstitutionFactory substitutionFactory, IntermediateQueryFactory iqFactory) {
        super();
        this.substitutionFactory = substitutionFactory;
        this.iqFactory = iqFactory;
    }


}
