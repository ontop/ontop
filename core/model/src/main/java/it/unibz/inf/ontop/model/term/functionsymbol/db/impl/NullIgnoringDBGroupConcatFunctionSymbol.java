package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;

import javax.annotation.Nonnull;

public class NullIgnoringDBGroupConcatFunctionSymbol extends AbstractDBAggregationFunctionSymbol {

    protected NullIgnoringDBGroupConcatFunctionSymbol(@Nonnull DBTermType dbStringType, boolean isDistinct,
                                                      @Nonnull DBFunctionSymbolSerializer serializer) {
        super("DB_GROUP_CONCAT", ImmutableList.of(dbStringType, dbStringType), dbStringType, isDistinct, serializer);
    }

    @Override
    public Constant evaluateEmptyBag(TermFactory termFactory) {
        return termFactory.getDBStringConstant("");
    }

    @Override
    protected boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    protected boolean tolerateNulls() {
        return true;
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return false;
    }
}
