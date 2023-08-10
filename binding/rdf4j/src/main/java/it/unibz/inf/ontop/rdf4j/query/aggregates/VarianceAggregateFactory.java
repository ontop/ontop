package it.unibz.inf.ontop.rdf4j.query.aggregates;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.parser.sparql.aggregate.AggregateCollector;
import org.eclipse.rdf4j.query.parser.sparql.aggregate.AggregateFunction;
import org.eclipse.rdf4j.query.parser.sparql.aggregate.AggregateFunctionFactory;

import java.util.function.Function;

public abstract class VarianceAggregateFactory implements AggregateFunctionFactory {

    /*
    These methods are not required for the parser. We only need to set the IRI of the corresponding functions.
     */

    @Override
    public AggregateFunction buildFunction(Function<BindingSet, Value> function) {
        return null;
    }

    @Override
    public AggregateCollector getCollector() {
        return null;
    }
}
