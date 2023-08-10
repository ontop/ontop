package it.unibz.inf.ontop.rdf4j.query.aggregates;

import it.unibz.inf.ontop.model.vocabulary.AGG;

public class StdevSampAggregateFactory extends StdevAggregateFactory {

    @Override
    public String getIri() {
        return AGG.STDEV_SAMP.getIRIString();
    }
}
