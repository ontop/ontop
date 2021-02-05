package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIsTrueFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class DefaultDBIsTrueFunctionSymbol extends DBBooleanFunctionSymbolImpl implements DBIsTrueFunctionSymbol {

    protected DefaultDBIsTrueFunctionSymbol(DBTermType dbBooleanTermType) {
        super("IS_TRUE", ImmutableList.of(dbBooleanTermType), dbBooleanTermType);
    }

    /**
     * TODO: shall we introduce an IS_FALSE?
     */
    @Override
    public boolean blocksNegation() {
        return true;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter,
                                    TermFactory termFactory) {
        return termConverter.apply(terms.get(0));
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return true;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        ImmutableTerm newTerm = newTerms.get(0);
        if (newTerm instanceof DBConstant) {
            return termFactory.getDBBooleanConstant(
                    evaluateDBConstant((DBConstant) newTerm, termFactory));
        }
        else if (newTerm instanceof ImmutableExpression)
            return newTerm;
        else
            return termFactory.getImmutableExpression(this, newTerm);
    }

    protected boolean evaluateDBConstant(DBConstant constant, TermFactory termFactory) {
        return constant.equals(termFactory.getDBBooleanConstant(true));
    }
}
