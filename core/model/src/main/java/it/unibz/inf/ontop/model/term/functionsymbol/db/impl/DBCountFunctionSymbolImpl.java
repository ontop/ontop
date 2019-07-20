package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;

import javax.annotation.Nonnull;

public class DBCountFunctionSymbolImpl extends AbstractDBAggregationFunctionSymbol {

    protected DBCountFunctionSymbolImpl(@Nonnull DBTermType inputType,
                                        @Nonnull DBTermType targetType,
                                        boolean isDistinct,
                                        @Nonnull DBFunctionSymbolSerializer serializer) {
        super(isDistinct ? "COUNT_DISTINCT_1" : "COUNT_1", ImmutableList.of(inputType), targetType, isDistinct, serializer);
    }

    protected DBCountFunctionSymbolImpl(@Nonnull DBTermType targetType,
                                        boolean isDistinct,
                                        @Nonnull DBFunctionSymbolSerializer serializer) {
        super(isDistinct ? "COUNT_DISTINCT_0" : "COUNT_0", ImmutableList.of(), targetType, isDistinct, serializer);
    }

    protected DBCountFunctionSymbolImpl(@Nonnull DBTermType inputType,
                                        @Nonnull DBTermType targetType, boolean isDistinct) {
        this(inputType, targetType, isDistinct,
                isDistinct
                        ? Serializers.getDistinctAggregationSerializer("COUNT")
                        : Serializers.getRegularSerializer("COUNT"));
    }

    protected DBCountFunctionSymbolImpl(@Nonnull DBTermType targetType, boolean isDistinct) {
        this(targetType, isDistinct, get0arySerializer(isDistinct));
    }

    protected static DBFunctionSymbolSerializer get0arySerializer(boolean isDistinct) {
        if (isDistinct)
            return (terms, termConverter, termFactory) -> "COUNT(DISTINCT(*))";
        else
            return (terms, termConverter, termFactory) -> "COUNT(*)";
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
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }
}
