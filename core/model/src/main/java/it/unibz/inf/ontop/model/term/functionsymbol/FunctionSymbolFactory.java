package it.unibz.inf.ontop.model.term.functionsymbol;


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
}
