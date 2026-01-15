package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.evaluator.QueryContext;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.QueryContextSimplifiableFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import javax.annotation.Nonnull;
import java.util.Optional;

public class QueryIdFunctionSymbol extends FunctionSymbolImpl implements QueryContextSimplifiableFunctionSymbol {
    private final DBTermType dbStringType;

    protected QueryIdFunctionSymbol(DBTermType dbStringType) {
        super("ontop_query_id", ImmutableList.of());
        this.dbStringType = dbStringType;
    }

    @Override
    public ImmutableTerm simplifyWithContext(ImmutableList<ImmutableTerm> terms, @Nonnull QueryContext queryContext, TermFactory termFactory) {
        return termFactory.getDBStringConstant(queryContext.getQueryId().toString());
    }

    @Override
    protected boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return true;
    }

    @Override
    protected boolean tolerateNulls() {
        return false;
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return false;
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(dbStringType));
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }
}
