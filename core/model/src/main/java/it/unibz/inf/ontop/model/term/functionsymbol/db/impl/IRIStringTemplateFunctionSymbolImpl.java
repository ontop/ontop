package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.EvaluationResult;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.NonNullConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.IRIStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.URITemplates;


public class IRIStringTemplateFunctionSymbolImpl extends ObjectStringTemplateFunctionSymbolImpl implements IRIStringTemplateFunctionSymbol {

    private IRIStringTemplateFunctionSymbolImpl(String iriTemplate, int arity, TypeFactory typeFactory) {
        super(iriTemplate, arity, typeFactory);
    }

    protected static IRIStringTemplateFunctionSymbol createFunctionSymbol(String iriTemplate,
                                                                          TypeFactory typeFactory) {
        int arity = URITemplates.getArity(iriTemplate);

        return new IRIStringTemplateFunctionSymbolImpl(iriTemplate, arity, typeFactory);
    }

    /**
     * TODO: try to reject or to decompose
     */
    @Override
    protected EvaluationResult evaluateStrictEqWithNonNullConstant(ImmutableList<? extends ImmutableTerm> terms,
                                                                   NonNullConstant otherTerm, TermFactory termFactory) {
        return super.evaluateStrictEqWithNonNullConstant(terms, otherTerm, termFactory);
    }
}
