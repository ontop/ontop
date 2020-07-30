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
public class NullIgnoringDBAvgFunctionSymbol extends AbstractDBAggregationFunctionSymbol {

    protected NullIgnoringDBAvgFunctionSymbol(@Nonnull DBTermType inputDBType,
                                              @Nonnull DBTermType targetDBType,
                                              boolean isDistinct,
                                              @Nonnull DBFunctionSymbolSerializer serializer) {
        super("AVG_" + inputDBType + (isDistinct ? "_DISTINCT" : ""), ImmutableList.of(inputDBType), targetDBType, isDistinct,
                serializer);
    }

    protected NullIgnoringDBAvgFunctionSymbol(@Nonnull DBTermType inputType, @Nonnull DBTermType targetType, boolean isDistinct) {
        this(inputType, targetType, isDistinct,
                isDistinct
                        ? Serializers.getDistinctAggregationSerializer("AVG")
                        : Serializers.getRegularSerializer("AVG"));
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
