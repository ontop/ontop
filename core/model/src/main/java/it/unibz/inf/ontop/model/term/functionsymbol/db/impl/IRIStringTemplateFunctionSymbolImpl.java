package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.template.TemplateComponent;
import it.unibz.inf.ontop.model.term.functionsymbol.db.IRIStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.type.TypeFactory;


public class IRIStringTemplateFunctionSymbolImpl extends ObjectStringTemplateFunctionSymbolImpl implements IRIStringTemplateFunctionSymbol {

    private IRIStringTemplateFunctionSymbolImpl(ImmutableList<TemplateComponent> template, TypeFactory typeFactory) {
        super(template, typeFactory);
    }

    protected static IRIStringTemplateFunctionSymbol createFunctionSymbol(ImmutableList<TemplateComponent> template,
                                                                          TypeFactory typeFactory) {
        return new IRIStringTemplateFunctionSymbolImpl(template, typeFactory);
    }
}
