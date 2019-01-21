package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;

public class MultiplySPARQLFunctionSymbolImpl extends AbstractNumericBinarySPARQLFunctionSymbol {

    protected MultiplySPARQLFunctionSymbolImpl(RDFDatatype abstractNumericType) {
        super("SP_MULTIPLY", SPARQL.NUMERIC_MULTIPLY, abstractNumericType);
    }

    @Override
    protected ImmutableTerm computeNumericTerm(ImmutableFunctionalTerm numericTerm1,
                                               ImmutableFunctionalTerm numericTerm2, TermFactory termFactory) {
        throw new RuntimeException("TODO: implement DB multiply");
    }
}
