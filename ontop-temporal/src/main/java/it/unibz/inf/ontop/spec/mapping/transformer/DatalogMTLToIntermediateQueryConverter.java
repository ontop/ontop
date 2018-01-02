package it.unibz.inf.ontop.spec.mapping.transformer;

import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.mapping.impl.IntervalAndIntermediateQuery;
import it.unibz.inf.ontop.temporal.model.DatalogMTLRule;

import java.util.Map;

public interface DatalogMTLToIntermediateQueryConverter {

    IntermediateQuery dMTLToIntermediateQuery(DatalogMTLRule rule,
                                              DBMetadata temporalDBMetadata, ExecutorRegistry executorRegistry);
}
