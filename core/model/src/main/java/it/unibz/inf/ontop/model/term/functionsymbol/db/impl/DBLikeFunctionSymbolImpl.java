package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.function.Function;


public class DBLikeFunctionSymbolImpl extends DBBooleanFunctionSymbolImpl {

    private static final String LIKE_TEMPLATE = "(%s LIKE %s)";

    /**
     * TODO: type the input
     */
    protected DBLikeFunctionSymbolImpl(DBTermType dbBooleanTermType, DBTermType rootDBType) {
        super("DB_LIKE", ImmutableList.of(rootDBType, rootDBType), dbBooleanTermType);
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
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format(LIKE_TEMPLATE, termConverter.apply(terms.get(0)), termConverter.apply(terms.get(1)));
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    /**
     * TODO: support post-processing
     */
    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
    }
}
