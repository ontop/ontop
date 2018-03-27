package it.unibz.inf.ontop.spec.mapping.transformer;

import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.MissingTemporalIntermediateQueryNodeException;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;

public interface RedundantTemporalCoalesceEliminator {
    IntermediateQuery removeRedundantTemporalCoalesces(IntermediateQuery intermediateQuery, DBMetadata temporalDBMetadata, ExecutorRegistry executorRegistry) throws MissingTemporalIntermediateQueryNodeException;
}
