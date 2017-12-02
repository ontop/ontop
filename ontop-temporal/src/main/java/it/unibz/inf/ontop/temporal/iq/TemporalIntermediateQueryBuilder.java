package it.unibz.inf.ontop.temporal.iq;

import it.unibz.inf.ontop.injection.TemporalIntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IntermediateQueryBuilder;

public interface TemporalIntermediateQueryBuilder extends IntermediateQueryBuilder{

    @Override
    TemporalIntermediateQueryFactory getFactory();
}
