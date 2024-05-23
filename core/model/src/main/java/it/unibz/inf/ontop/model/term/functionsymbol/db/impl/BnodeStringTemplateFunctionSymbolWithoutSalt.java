package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.evaluator.QueryContext;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.template.Template;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.BnodeStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.QueryContextSimplifiableFunctionSymbol;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nullable;
import java.util.function.Function;

public class BnodeStringTemplateFunctionSymbolWithoutSalt extends ObjectStringTemplateFunctionSymbolImpl
        implements BnodeStringTemplateFunctionSymbol, QueryContextSimplifiableFunctionSymbol {

    private BnodeStringTemplateFunctionSymbolWithoutSalt(ImmutableList<Template.Component> template, TypeFactory typeFactory) {
        super(template, typeFactory);
    }

    /**
     * Blocks the simplification into constants
     */
    @Override
    protected ImmutableTerm simplifyWithAllParametersConstant(ImmutableList<DBConstant> newTerms,
                                                              TermFactory termFactory,
                                                              VariableNullability variableNullability) {
        return termFactory.getImmutableFunctionalTerm(this, newTerms);
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter,
                                    TermFactory termFactory) {
        throw new UnsupportedOperationException("BnodeStringTemplateFunctionSymbolWithoutSalt cannot be serialized in a native string. " +
                "Is expected to be replaced by a BnodeStringTemplateFunctionSymbolWithSalt");
    }

    /**
     * NB: assumes that all BnodeStringTemplateFunctionSymbolWithoutSalt are replaced at the same time by
     * BnodeStringTemplateFunctionSymbolWithSalt.
     * Therefore, there is no chance to match with a constant, as BnodeStringTemplateFunctionSymbolWithoutSalt-s
     * do not simplify into constants (only BnodeStringTemplateFunctionSymbolWithSalt-s do)
     *
     */
    @Override
    protected IncrementalEvaluation evaluateStrictEqWithNonNullConstant(ImmutableList<? extends ImmutableTerm> terms,
                                                                        NonNullConstant otherTerm, TermFactory termFactory,
                                                                        VariableNullability variableNullability) {
        return termFactory.getFalseOrNullFunctionalTerm(
                        terms.stream()
                                .map(termFactory::getDBIsNotNull)
                                .collect(ImmutableCollectors.toList()))
                .evaluate(variableNullability, true);

    }

    public static BnodeStringTemplateFunctionSymbol createFunctionSymbol(ImmutableList<Template.Component> template,
                                                                         TypeFactory typeFactory) {
        return new BnodeStringTemplateFunctionSymbolWithoutSalt(template, typeFactory);
    }

    @Override
    public ImmutableFunctionalTerm simplifyWithContext(ImmutableList<ImmutableTerm> terms, @Nullable QueryContext queryContext, TermFactory termFactory) {
        if (queryContext == null)
            return termFactory.getImmutableFunctionalTerm(this, terms);

        var newFunctionSymbol = new BnodeStringTemplateFunctionSymbolWithSalt(getTemplateComponents(),
                queryContext.getSalt(),
                termFactory.getTypeFactory());

        return termFactory.getImmutableFunctionalTerm(newFunctionSymbol, terms);
    }
}
