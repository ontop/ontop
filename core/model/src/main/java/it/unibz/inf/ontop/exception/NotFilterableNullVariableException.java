package it.unibz.inf.ontop.exception;

import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.iq.IntermediateQuery;

public class NotFilterableNullVariableException extends QueryTransformationException {

    public NotFilterableNullVariableException(IntermediateQuery query, Variable nullVariable) {
        super("The variable " + nullVariable + " is null and cannot be filtered.Query:\n" + query);
    }

    public NotFilterableNullVariableException(IntermediateQuery query, ImmutableExpression expression) {
        super(expression + " is evaluated as false, making the query being empty. " +
                "Query:\n" + query);
    }
}
