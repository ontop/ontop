package it.unibz.inf.ontop.model.term.functionsymbol.db;

import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;


public interface DBFunctionSymbol extends FunctionSymbol, DBFunctionSymbolSerializer {

    /**
     * If it is better to be post-processed than being blocked behind an UNION.
     *
     * Obviously, non post-processable function symbols must return false.
     *
     */
    boolean isPreferringToBePostProcessedOverBeingBlocked();
}
