package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.functionsymbol.db.BnodeStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.type.TypeFactory;

public class BnodeStringTemplateFunctionSymbolImpl extends ObjectStringTemplateFunctionSymbolImpl
        implements BnodeStringTemplateFunctionSymbol {

    private BnodeStringTemplateFunctionSymbolImpl(ImmutableList<Template.Component> template, TypeFactory typeFactory) {
        super(template, typeFactory);
    }

    public static BnodeStringTemplateFunctionSymbol createFunctionSymbol(ImmutableList<Template.Component> template,
                                                                         TypeFactory typeFactory) {
        return new BnodeStringTemplateFunctionSymbolImpl(template, typeFactory);
    }
}
