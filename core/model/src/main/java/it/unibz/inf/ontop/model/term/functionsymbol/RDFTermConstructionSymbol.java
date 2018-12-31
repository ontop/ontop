package it.unibz.inf.ontop.model.term.functionsymbol;

import it.unibz.inf.ontop.model.type.RDFTermType;

/**
 * Symbol of (non-SPARQL) functions that construct RDF terms
 */
public interface RDFTermConstructionSymbol extends FunctionSymbol {

    RDFTermType getReturnedType();
}
