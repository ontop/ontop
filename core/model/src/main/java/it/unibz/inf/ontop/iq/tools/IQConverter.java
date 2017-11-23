package it.unibz.inf.ontop.iq.tools;

import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IntermediateQuery;

public interface IQConverter {

    IQ convert(IntermediateQuery query);

    IntermediateQuery convert(IQ query, DBMetadata dbMetadata, ExecutorRegistry executorRegistry);
}
