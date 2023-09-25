package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.vocabulary.AGG;

public class VarianceSPARQLFunctionSymbolImpl extends AbstractStatisticalSPARQLAggregationFunctionSymbolImpl {

    private static final String DEFAULT_AGG_VAR_NAME = "variance";

    protected VarianceSPARQLFunctionSymbolImpl(RDFTermType rootRdfTermType, boolean isPop, boolean isDistinct) {
        this(rootRdfTermType, isPop, isDistinct, false);
    }

    protected VarianceSPARQLFunctionSymbolImpl(RDFTermType rootRdfTermType, boolean isDistinct) {
        this(rootRdfTermType, false, isDistinct, true);
    }

    private VarianceSPARQLFunctionSymbolImpl(RDFTermType rootRdfTermType, boolean isPop, boolean isDistinct, boolean shortName) {
        super(DEFAULT_AGG_VAR_NAME, "SP_VARIANCE", shortName ? AGG.VARIANCE.getIRIString() : (isPop ? AGG.VAR_POP.getIRIString() : AGG.VAR_SAMP.getIRIString()), rootRdfTermType, isDistinct, isPop, shortName,
                ((sparqlFunction, termFactory, dbTerm, dbType) -> termFactory.getDBVariance(dbTerm, dbType, sparqlFunction.isPop(), sparqlFunction.isDistinct())));
    }

}
