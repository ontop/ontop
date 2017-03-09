package it.unibz.inf.ontop.pivotalrepr.transform;


import it.unibz.inf.ontop.exception.NotFilterableNullVariableException;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;

public interface FilterNullableVariableQueryTransformer extends QueryTransformer {

    @Override
    IntermediateQuery transform(IntermediateQuery query) throws NotFilterableNullVariableException;
}
