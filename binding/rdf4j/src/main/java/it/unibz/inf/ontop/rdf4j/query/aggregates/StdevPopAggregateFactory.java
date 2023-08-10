package it.unibz.inf.ontop.rdf4j.query.aggregates;

import it.unibz.inf.ontop.model.vocabulary.AGG;

public class StdevPopAggregateFactory extends StdevAggregateFactory {

    @Override
    public String getIri() {
        return AGG.STDEV_POP.getIRIString();
    }
}
