package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;

/**
 * IF cond THEN v1 ELSE v2
 *
 * TODO: simplify strict equalities
 *
 */
public abstract class AbstractDBIfThenElseFunctionSymbol extends AbstractDBIfThenFunctionSymbol {

    protected AbstractDBIfThenElseFunctionSymbol(DBTermType dbBooleanType, DBTermType rootDBTermType) {
        super("IF_THEN_ELSE", 3, dbBooleanType, rootDBTermType, false);
    }

    @Override
    protected ImmutableTerm extractDefaultValue(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory) {
        return terms.get(2);
    }

    @Override
    public ImmutableTerm simplify(ImmutableList<? extends ImmutableTerm> terms,
                                  TermFactory termFactory, VariableNullability variableNullability) {
        ImmutableTerm defaultValue = extractDefaultValue(terms, termFactory);

        if (defaultValue.equals(termFactory.getNullConstant()))
            return termFactory.getIfElseNull((ImmutableExpression) terms.get(0), terms.get(1))
                    .simplify(variableNullability);

        if (terms.get(1).equals(defaultValue))
            return defaultValue;

        return super.simplify(terms, termFactory, variableNullability);
    }

    /**
     * Only looks if the second and third argument are guaranteed to be post-processed or not
     * since the first argument (the condition expression) will always be evaluated.
     */
    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return extractSubFunctionalTerms(arguments.subList(1, 3))
                .allMatch(ImmutableFunctionalTerm::canBePostProcessed);
    }
}
