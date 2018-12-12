package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;

import static it.unibz.inf.ontop.model.term.functionsymbol.BooleanExpressionOperation.IS_NOT_NULL;

public abstract class AbstractDBIfElseNullFunctionSymbol extends AbstractDBIfThenFunctionSymbol {

    protected AbstractDBIfElseNullFunctionSymbol(DBTermType dbBooleanType, DBTermType rootDBTermType) {
        super("IF_ELSE_NULL", 2, dbBooleanType, rootDBTermType);
    }

    @Override
    protected ImmutableTerm extractDefaultValue(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory) {
        return termFactory.getNullConstant();
    }

    @Override
    public ImmutableTerm simplify(ImmutableList<? extends ImmutableTerm> terms, boolean isInConstructionNodeInOptimizationPhase, TermFactory termFactory) {
        ImmutableTerm possibleValue = terms.get(1);
        /*
         * Optimizes the special case IF_ELSE_NULL(IS_NOT_NULL(x),x) === x
         */
        if (possibleValue instanceof Variable) {
            ImmutableExpression expression = (ImmutableExpression) terms.get(0);
            if ((expression.getFunctionSymbol() == IS_NOT_NULL) && expression.getTerm(0).equals(possibleValue))
                return possibleValue;
        }
        return super.simplify(terms, isInConstructionNodeInOptimizationPhase, termFactory);
    }
}
