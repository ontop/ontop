package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import it.unibz.inf.ontop.model.term.functionsymbol.db.BnodeStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.Templates;

public class BnodeStringTemplateFunctionSymbolImpl extends ObjectStringTemplateFunctionSymbolImpl
        implements BnodeStringTemplateFunctionSymbol {

    private BnodeStringTemplateFunctionSymbolImpl(String template, TypeFactory typeFactory) {
        super(template, typeFactory);
    }

    public static BnodeStringTemplateFunctionSymbol createFunctionSymbol(String template,
                                                                         TypeFactory typeFactory) {
        return new BnodeStringTemplateFunctionSymbolImpl(template, typeFactory);
    }
}
