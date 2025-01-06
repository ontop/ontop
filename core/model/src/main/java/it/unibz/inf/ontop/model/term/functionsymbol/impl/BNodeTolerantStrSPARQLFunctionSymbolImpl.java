package it.unibz.inf.ontop.model.term.functionsymbol.impl;


import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.RDFTermType;

/**
 * For internal usage only!
 * Not made available to SPARQL users, as it would leak blank node labels (which may contain sensitive identifiers)
 */
public class BNodeTolerantStrSPARQLFunctionSymbolImpl extends AbstractStrSPARQLFunctionSymbolImpl {

    private static final String NAME = "INTERNAL_SP_BNODE_TOLERANT_STR";

    protected BNodeTolerantStrSPARQLFunctionSymbolImpl(RDFTermType abstractRDFType, RDFDatatype xsdStringType) {
        super(NAME, NAME, abstractRDFType, xsdStringType);
    }
}
