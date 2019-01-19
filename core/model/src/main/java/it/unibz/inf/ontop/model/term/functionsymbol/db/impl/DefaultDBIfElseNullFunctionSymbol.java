package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.function.Function;

public class DefaultDBIfElseNullFunctionSymbol extends AbstractDBIfThenFunctionSymbol {

    protected DefaultDBIfElseNullFunctionSymbol(DBTermType dbBooleanType, DBTermType rootDBTermType) {
        super("IF_ELSE_NULL", 2, dbBooleanType, rootDBTermType);
    }

    @Override
    protected ImmutableTerm extractDefaultValue(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory) {
        return termFactory.getNullConstant();
    }

    @Override
    public ImmutableTerm simplify(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory, VariableNullability variableNullability) {
        ImmutableTerm possibleValue = terms.get(1);

        if (possibleValue.equals(termFactory.getNullConstant()))
            return possibleValue;

        /*
         * Optimizes the special case IF_ELSE_NULL(IS_NOT_NULL(x),x) === x
         */
        if (possibleValue instanceof Variable) {
            ImmutableExpression expression = (ImmutableExpression) terms.get(0);
            // TODO: make it more efficient?
            if (expression.equals(termFactory.getDBIsNotNull(possibleValue)))
                return possibleValue;
        }
        return super.simplify(terms, termFactory, variableNullability);
    }

    /**
     * Only looks if the second argument is guaranteed to be post-processed or not
     * since the first argument (the condition expression) will always be evaluated.
     */
    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return extractSubFunctionalTerms(arguments.subList(1, 2))
                .allMatch(ImmutableFunctionalTerm::canBePostProcessed);
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter,
                                    TermFactory termFactory) {
        return termConverter.apply(
                termFactory.getIfThenElse(
                        (ImmutableExpression) terms.get(0),
                        terms.get(1),
                        termFactory.getNullConstant()));
    }
}
