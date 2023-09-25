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
public class NullIgnoringDBStdevFunctionSymbol extends AbstractDBAggregationFunctionSymbol {

    private final boolean isPop;

    protected NullIgnoringDBStdevFunctionSymbol(@Nonnull DBTermType inputDBType,
                                                @Nonnull DBTermType targetDBType,
                                                boolean isPop,
                                                boolean isDistinct,
                                                @Nonnull DBFunctionSymbolSerializer serializer) {
        super("STDEV_" + inputDBType + (isPop ? "_POP" : "_SAMPLE") + (isDistinct ? "_DISTINCT" : ""), ImmutableList.of(inputDBType), targetDBType, isDistinct,
                serializer);
        this.isPop = isPop;
    }

    protected NullIgnoringDBStdevFunctionSymbol(@Nonnull DBTermType inputType, @Nonnull DBTermType targetType, boolean isPop, boolean isDistinct) {
        this("STDDEV_" + (isPop ? "POP" : "SAMP"), inputType, targetType, isPop, isDistinct);
    }

    protected NullIgnoringDBStdevFunctionSymbol(@Nonnull String nameInDialect, @Nonnull DBTermType inputType, @Nonnull DBTermType targetType, boolean isPop, boolean isDistinct) {
        this(inputType, targetType, isPop, isDistinct,
                isDistinct
                        ? Serializers.getDistinctAggregationSerializer(nameInDialect)
                        : Serializers.getRegularSerializer(nameInDialect));
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
