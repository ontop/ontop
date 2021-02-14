package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.functionsymbol.db.IRIStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.type.TypeFactory;


public class IRIStringTemplateFunctionSymbolImpl extends ObjectStringTemplateFunctionSymbolImpl implements IRIStringTemplateFunctionSymbol {

    private IRIStringTemplateFunctionSymbolImpl(ImmutableList<Template.Component> template, TypeFactory typeFactory) {
        super(template, typeFactory);
    }

    protected static IRIStringTemplateFunctionSymbol createFunctionSymbol(ImmutableList<Template.Component> template,
                                                                          TypeFactory typeFactory) {
        return new IRIStringTemplateFunctionSymbolImpl(template, typeFactory);
    }
}
