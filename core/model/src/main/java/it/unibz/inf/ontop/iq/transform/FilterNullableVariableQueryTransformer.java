package it.unibz.inf.ontop.iq.transform;


import it.unibz.inf.ontop.exception.NotFilterableNullVariableException;
import it.unibz.inf.ontop.iq.IntermediateQuery;

public interface FilterNullableVariableQueryTransformer extends QueryTransformer {

    @Override
    IntermediateQuery transform(IntermediateQuery query) throws NotFilterableNullVariableException;
}
