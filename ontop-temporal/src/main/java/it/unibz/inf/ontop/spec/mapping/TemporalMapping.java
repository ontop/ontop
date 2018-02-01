package it.unibz.inf.ontop.spec.mapping;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.spec.mapping.impl.IntervalAndIntermediateQuery;

public interface TemporalMapping extends Mapping {
    IntervalAndIntermediateQuery getIntervalAndIntermediateQuery(AtomPredicate predicate);
    ImmutableMap<AtomPredicate, IntervalAndIntermediateQuery> getDefinitions();
}
