package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;

public class IsNumericSPARQLFunctionSymbolImpl extends AbstractIsASPARQLFunctionSymbol {
    protected IsNumericSPARQLFunctionSymbolImpl(RDFDatatype abstractRDFNumericType, RDFTermType rootRDFType, RDFDatatype xsdBooleanType) {
        super("SP_IS_NUMERIC", SPARQL.IS_NUMERIC, abstractRDFNumericType, rootRDFType, xsdBooleanType);
    }
}
