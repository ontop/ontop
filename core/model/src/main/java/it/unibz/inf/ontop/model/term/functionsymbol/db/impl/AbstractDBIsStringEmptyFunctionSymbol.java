package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

public abstract class AbstractDBIsStringEmptyFunctionSymbol extends DBBooleanFunctionSymbolImpl {

    protected AbstractDBIsStringEmptyFunctionSymbol(DBTermType dbBooleanType, DBTermType rootDBType) {
        super("IS_STR_EMPTY", ImmutableList.of(rootDBType), dbBooleanType);
    }

    @Override
    public boolean blocksNegation() {
        return true;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     TermFactory termFactory, VariableNullability variableNullability) {
        ImmutableTerm newTerm = newTerms.get(0);
        if (newTerm instanceof DBConstant)
            return termFactory.getDBBooleanConstant(((DBConstant) newTerm).getValue().isEmpty());

        return super.buildTermAfterEvaluation(newTerms, termFactory, variableNullability);
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
    }
}
