package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import it.unibz.inf.ontop.model.term.functionsymbol.IRIStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.URITemplates;


public class IRIStringTemplateFunctionSymbolImpl extends ObjectStringTemplateFunctionSymbolImpl implements IRIStringTemplateFunctionSymbol {
    private final TermType returnType;
    private final String iriTemplate;

    private IRIStringTemplateFunctionSymbolImpl(String iriTemplate, int arity, TypeFactory typeFactory) {
        super(iriTemplate, arity, typeFactory);
        // TODO: require DB string instead
        this.returnType = typeFactory.getXsdStringDatatype();
        this.iriTemplate = iriTemplate;
    }

    protected static IRIStringTemplateFunctionSymbol createFunctionSymbol(String iriTemplate,
                                                                          TypeFactory typeFactory) {
        int arity = URITemplates.getArity(iriTemplate);

        return new IRIStringTemplateFunctionSymbolImpl(iriTemplate, arity, typeFactory);
    }

    @Override
    public String getTemplate() {
        return iriTemplate;
    }
}
