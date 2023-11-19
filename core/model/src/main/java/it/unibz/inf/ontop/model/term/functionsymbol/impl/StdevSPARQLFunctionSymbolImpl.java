package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.vocabulary.AGG;

public class StdevSPARQLFunctionSymbolImpl extends AbstractStatisticalSPARQLAggregationFunctionSymbolImpl {

    private static final String DEFAULT_AGG_VAR_NAME = "stdev1";

    protected StdevSPARQLFunctionSymbolImpl(RDFTermType rootRdfTermType, boolean isPop, boolean isDistinct) {
        this(rootRdfTermType, isPop, isDistinct, false);
    }

    protected StdevSPARQLFunctionSymbolImpl(RDFTermType rootRdfTermType, boolean isDistinct) {
        this(rootRdfTermType, false, isDistinct, true);
    }

    private StdevSPARQLFunctionSymbolImpl(RDFTermType rootRdfTermType, boolean isPop, boolean isDistinct, boolean shortName) {
        super(DEFAULT_AGG_VAR_NAME, "SP_STDEV", shortName ? AGG.STDEV.getIRIString() : (isPop ? AGG.STDEV_POP.getIRIString() : AGG.STDEV_SAMP.getIRIString()), rootRdfTermType, isDistinct, isPop, shortName,
            ((sparqlFunction, termFactory, dbTerm, dbType) -> termFactory.getDBStdev(dbTerm, dbType, sparqlFunction.isPop(), sparqlFunction.isDistinct())));
    }


}
