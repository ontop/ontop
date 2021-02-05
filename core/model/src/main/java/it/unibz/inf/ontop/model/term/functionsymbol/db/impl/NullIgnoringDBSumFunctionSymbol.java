package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;

import javax.annotation.Nonnull;

/**
 * Ignores nulls.
 * Returns NULL if the bag/set does not contain any non-null value.
 */
public class NullIgnoringDBSumFunctionSymbol extends AbstractDBAggregationFunctionSymbol {

    protected NullIgnoringDBSumFunctionSymbol(@Nonnull DBTermType dbType,
                                              boolean isDistinct,
                                              @Nonnull DBFunctionSymbolSerializer serializer) {
        super("SUM_" + dbType + (isDistinct ? "_DISTINCT" : ""), ImmutableList.of(dbType), dbType, isDistinct,
                serializer);
    }

    protected NullIgnoringDBSumFunctionSymbol(@Nonnull DBTermType inputType, boolean isDistinct) {
        this(inputType, isDistinct,
                isDistinct
                        ? Serializers.getDistinctAggregationSerializer("SUM")
                        : Serializers.getRegularSerializer("SUM"));
    }

    @Override
    protected boolean tolerateNulls() {
        return true;
    }

    /**
     * When the bag/set does not contain any null
     */
    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return true;
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public Constant evaluateEmptyBag(TermFactory termFactory) {
        return termFactory.getNullConstant();
    }
}
