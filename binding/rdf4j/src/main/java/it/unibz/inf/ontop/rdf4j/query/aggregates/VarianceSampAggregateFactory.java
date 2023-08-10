package it.unibz.inf.ontop.rdf4j.query.aggregates;

import it.unibz.inf.ontop.model.vocabulary.AGG;

public class VarianceSampAggregateFactory extends VarianceAggregateFactory {
    @Override
    public String getIri() {
        return AGG.VAR_SAMP.getIRIString();
    }
}
