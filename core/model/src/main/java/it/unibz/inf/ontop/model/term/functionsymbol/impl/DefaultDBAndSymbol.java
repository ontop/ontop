package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;

public class DefaultDBAndSymbol extends DBBooleanFunctionSymbolImpl {

    protected DefaultDBAndSymbol(String nameInDialect, int arity, DBTermType dbBooleanTermType) {
        super(nameInDialect, arity, dbBooleanTermType);
        if (arity < 2)
            throw new IllegalArgumentException("Arity must be >= 2");
    }

    @Override
    public boolean isInjective(ImmutableList<? extends ImmutableTerm> arguments, ImmutableSet<Variable> nonNullVariables) {
        return false;
    }

    @Override
    public boolean canBePostProcessed() {
        return true;
    }

    /**
     * NB: terms are assumed to be either TRUE, FALSE, NULL or ImmutableExpressions.
     * TODO: check it
     */
    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     boolean isInConstructionNodeInOptimizationPhase,
                                                     TermFactory termFactory) {
        DBConstant falseValue = termFactory.getDBBooleanConstant(false);
        if (newTerms.stream()
                .anyMatch(falseValue::equals))
            return falseValue;

        Optional<ImmutableTerm> optionalNull = newTerms.stream()
                .filter(t -> (t instanceof Constant) && ((Constant) t).isNull())
                .findFirst();

        ImmutableList<ImmutableExpression> others = newTerms.stream()
                // We don't care about TRUE
                .filter(t -> (t instanceof ImmutableExpression))
                .map(t -> (ImmutableExpression) t)
                .collect(ImmutableCollectors.toList());

        return others.isEmpty()
                ? optionalNull.orElseGet(() -> termFactory.getDBBooleanConstant(true))
                :  optionalNull
                    .map(n -> (ImmutableTerm) termFactory.getFalseOrNullFunctionalTerm(others))
                    .orElseGet(() -> others.size() == 1 ? others.get(0) : termFactory.getConjunction(others));
    }
}
