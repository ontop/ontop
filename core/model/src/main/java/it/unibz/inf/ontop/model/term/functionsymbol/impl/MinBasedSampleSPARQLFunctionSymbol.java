package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;

/**
 * SAMPLE implemented as MIN
 */
public class MinBasedSampleSPARQLFunctionSymbol extends MinOrMaxSPARQLFunctionSymbolImpl {

    protected MinBasedSampleSPARQLFunctionSymbol(TypeFactory typeFactory) {
        super("SP_SAMPLE", SPARQL.SAMPLE, typeFactory, false);
    }
}
