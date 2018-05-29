package it.unibz.inf.ontop.model.term.functionsymbol;


public interface FunctionSymbolFactory {

    FunctionSymbol getRDFTermFunctionSymbol();

    /**
     * NB: a functional term using this symbol is producing a NULL or a DB string
     */
    IRIStringTemplateFunctionSymbol getIRIStringTemplateFunctionSymbol(String iriTemplate);
}
