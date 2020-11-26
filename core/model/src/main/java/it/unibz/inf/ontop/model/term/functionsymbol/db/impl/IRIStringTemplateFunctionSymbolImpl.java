package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import it.unibz.inf.ontop.model.term.functionsymbol.db.IRIStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.type.TypeFactory;


public class IRIStringTemplateFunctionSymbolImpl extends ObjectStringTemplateFunctionSymbolImpl implements IRIStringTemplateFunctionSymbol {

    private IRIStringTemplateFunctionSymbolImpl(String iriTemplate, TypeFactory typeFactory) {
        super(iriTemplate, typeFactory);
    }

    protected static IRIStringTemplateFunctionSymbol createFunctionSymbol(String iriTemplate,
                                                                          TypeFactory typeFactory) {
        return new IRIStringTemplateFunctionSymbolImpl(iriTemplate, typeFactory);
    }
}
