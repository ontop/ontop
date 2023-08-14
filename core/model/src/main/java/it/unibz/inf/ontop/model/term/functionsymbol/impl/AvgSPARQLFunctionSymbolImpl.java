package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;


public class AvgSPARQLFunctionSymbolImpl extends AbstractStatisticalSPARQLAggregationFunctionSymbolImpl {

    private static final String DEFAULT_AGG_VAR_NAME = "avg1";

    protected AvgSPARQLFunctionSymbolImpl(RDFTermType rootRdfTermType, boolean isDistinct) {
        super(DEFAULT_AGG_VAR_NAME, "SP_AVG", SPARQL.AVG, rootRdfTermType, isDistinct, false,
                ((sparqlFunction, termFactory, dbTerm, dbType) -> termFactory.getDBAvg(dbTerm, dbType, sparqlFunction.isDistinct())));
    }
}
