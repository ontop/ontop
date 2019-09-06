package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import it.unibz.inf.ontop.model.term.functionsymbol.db.IRIStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.Templates;


public class IRIStringTemplateFunctionSymbolImpl extends ObjectStringTemplateFunctionSymbolImpl implements IRIStringTemplateFunctionSymbol {

    private IRIStringTemplateFunctionSymbolImpl(String iriTemplate, int arity, TypeFactory typeFactory) {
        super(iriTemplate, arity, typeFactory);
    }

    protected static IRIStringTemplateFunctionSymbol createFunctionSymbol(String iriTemplate,
                                                                          TypeFactory typeFactory) {
        int arity = Templates.getArity(iriTemplate);

        return new IRIStringTemplateFunctionSymbolImpl(iriTemplate, arity, typeFactory);
    }
}
