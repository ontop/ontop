package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.template.TemplateComponent;
import it.unibz.inf.ontop.model.term.functionsymbol.db.BnodeStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.type.TypeFactory;

public class BnodeStringTemplateFunctionSymbolImpl extends ObjectStringTemplateFunctionSymbolImpl
        implements BnodeStringTemplateFunctionSymbol {

    private BnodeStringTemplateFunctionSymbolImpl(ImmutableList<TemplateComponent> template, TypeFactory typeFactory) {
        super(template, typeFactory);
    }

    public static BnodeStringTemplateFunctionSymbol createFunctionSymbol(ImmutableList<TemplateComponent> template,
                                                                         TypeFactory typeFactory) {
        return new BnodeStringTemplateFunctionSymbolImpl(template, typeFactory);
    }
}
