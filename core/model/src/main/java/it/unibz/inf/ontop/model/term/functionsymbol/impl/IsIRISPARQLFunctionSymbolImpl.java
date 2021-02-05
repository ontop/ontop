package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import it.unibz.inf.ontop.model.type.ObjectRDFType;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;


public class IsIRISPARQLFunctionSymbolImpl extends AbstractIsASPARQLFunctionSymbol {
    protected IsIRISPARQLFunctionSymbolImpl(ObjectRDFType iriRDFType, RDFTermType rootRDFType, RDFDatatype xsdBooleanType) {
        super("SP_IS_IRI", SPARQL.IS_IRI, iriRDFType, rootRDFType, xsdBooleanType);
    }
}
