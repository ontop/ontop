package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.BnodeStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.util.function.Function;

public class BnodeStringTemplateFunctionSymbolWithSalt extends ObjectStringTemplateFunctionSymbolImpl
        implements BnodeStringTemplateFunctionSymbol {

    private BnodeStringTemplateFunctionSymbolWithSalt(ImmutableList<Template.Component> template, TypeFactory typeFactory) {
        super(template, typeFactory);
    }

    @Override
    protected ImmutableTerm simplifyWithAllParametersConstant(ImmutableList<DBConstant> newTerms,
                                                              TermFactory termFactory,
                                                              VariableNullability variableNullability) {
        String label = buildString(newTerms, termFactory, variableNullability);
        throw new RuntimeException("TODO: implement hashing with salt");
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter,
                                    TermFactory termFactory) {
        String labelSQLExpression = super.getNativeDBString(terms, termConverter, termFactory);
        throw new RuntimeException("TODO: implement hashing with salt");
    }

    public static BnodeStringTemplateFunctionSymbol createFunctionSymbol(ImmutableList<Template.Component> template,
                                                                         TypeFactory typeFactory) {
        // TODO: add salt
        return new BnodeStringTemplateFunctionSymbolWithSalt(template, typeFactory);
    }
}
