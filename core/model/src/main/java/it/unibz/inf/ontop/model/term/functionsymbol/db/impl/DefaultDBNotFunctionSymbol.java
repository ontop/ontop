package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBNotFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class DefaultDBNotFunctionSymbol extends DBBooleanFunctionSymbolImpl implements DBNotFunctionSymbol {

    private final String prefix;

    protected DefaultDBNotFunctionSymbol(@Nonnull String nameInDialect, DBTermType dbBooleanTermType) {
        super(nameInDialect, ImmutableList.of(dbBooleanTermType), dbBooleanTermType);
        prefix = String.format("%s ", nameInDialect);
    }

    @Override
    public boolean blocksNegation() {
        return false;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        return (ImmutableExpression) subTerms.get(0);
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter,
                                    TermFactory termFactory) {
        return inBrackets(prefix + termConverter.apply(terms.get(0)));
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
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     TermFactory termFactory, VariableNullability variableNullability) {

        ImmutableTerm newTerm = newTerms.get(0);
        if (newTerm instanceof Constant)
            return newTerm.isNull()
                    ? newTerm
                    : termFactory.getDBBooleanConstant(newTerm.equals(termFactory.getDBBooleanConstant(false)));

        if (newTerm instanceof ImmutableExpression)
            return ((ImmutableExpression) newTerm).negate(termFactory);
        else if (newTerm instanceof Variable)
            // TODO: shall we use NOT(IS_TRUE(...) instead)?
            return termFactory.getStrictEquality(newTerm, termFactory.getDBBooleanConstant(false));
        else
            throw new MinorOntopInternalBugException("NOT was expecting an expression as parameter");
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
    }
}
