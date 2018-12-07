package it.unibz.inf.ontop.model.term.functionsymbol;


import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.RDFTermTypeConstant;

import java.util.Optional;

public interface FunctionSymbolFactory {

    RDFTermFunctionSymbol getRDFTermFunctionSymbol();

    /**
     * NB: a functional term using this symbol is producing a NULL or a DB string
     */
    IRIStringTemplateFunctionSymbol getIRIStringTemplateFunctionSymbol(String iriTemplate);

    /**
     * NB: a functional term using this symbol is producing a NULL or a DB string
     */
    BnodeStringTemplateFunctionSymbol getBnodeStringTemplateFunctionSymbol(String bnodeTemplate);

    /**
     * Returns a fresh Bnode template
     */
    BnodeStringTemplateFunctionSymbol getFreshBnodeStringTemplateFunctionSymbol(int arity);


    DBFunctionSymbolFactory getDBFunctionSymbolFactory();

    BooleanFunctionSymbol isARDFTermTypeFunctionSymbol();

    MetaRDFTermTypeFunctionSymbol getMetaRDFTermTypeFunctionSymbol(
            ImmutableMap<DBConstant, RDFTermTypeConstant> conversionMap);

    // SPARQL functions

    Optional<SPARQLFunctionSymbol> getSPARQLFunctionSymbol(String officialName, int arity);

    FunctionSymbol getCommonDenominatorFunctionSymbol(int arity);
}
