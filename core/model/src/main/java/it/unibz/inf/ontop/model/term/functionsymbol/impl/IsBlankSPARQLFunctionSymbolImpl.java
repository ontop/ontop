package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;

public class IsBlankSPARQLFunctionSymbolImpl extends AbstractIsASPARQLFunctionSymbol {
    protected IsBlankSPARQLFunctionSymbolImpl(ObjectRDFType bnodeRDFType, RDFTermType rootRDFType, RDFDatatype xsdBooleanType) {
        super("SP_IS_BLANK", SPARQL.IS_BLANK, bnodeRDFType, rootRDFType, xsdBooleanType);
    }
}
