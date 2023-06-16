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
public class DBSampleFunctionSymbolImpl extends AbstractDBAggregationFunctionSymbol {

    protected DBSampleFunctionSymbolImpl(@Nonnull DBTermType dbType,
                                         @Nonnull DBFunctionSymbolSerializer serializer) {
        super("SAMPLE_" + dbType, ImmutableList.of(dbType), dbType, false,
                serializer);
    }

    protected DBSampleFunctionSymbolImpl(@Nonnull DBTermType inputType, String functionName) {
        this(inputType, Serializers.getRegularSerializer(functionName));
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
