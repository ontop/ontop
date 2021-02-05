package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;

public class IsLiteralSPARQLFunctionSymbolImpl extends AbstractIsASPARQLFunctionSymbol {

    protected IsLiteralSPARQLFunctionSymbolImpl(RDFTermType rdfsLiteralType, RDFTermType rootRDFType, RDFDatatype xsdBooleanType) {
        super("SP_IS_LITERAL", SPARQL.IS_LITERAL, rdfsLiteralType, rootRDFType, xsdBooleanType);
    }
}
