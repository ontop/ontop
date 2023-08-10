package it.unibz.inf.ontop.rdf4j.query.aggregates;

import it.unibz.inf.ontop.model.vocabulary.AGG;

public class VariancePopAggregateFactory extends VarianceAggregateFactory {
    @Override
    public String getIri() {
        return AGG.VAR_POP.getIRIString();
    }
}
