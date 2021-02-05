package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.function.Function;

public class NonSimplifiableTypedNullFunctionSymbol extends AbstractTypedDBFunctionSymbol {

    /**
     * May differ from the target type (e.g. INTEGER for SERIAL)
     */
    private final DBTermType castingType;

    protected NonSimplifiableTypedNullFunctionSymbol(DBTermType targetType) {
        super("NULL-" + targetType, ImmutableList.of(), targetType);
        castingType = targetType;
    }

    protected NonSimplifiableTypedNullFunctionSymbol(DBTermType targetType, DBTermType castingType) {
        super("NULL-" + castingType, ImmutableList.of(), targetType);
        this.castingType = castingType;
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return true;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }


    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return termConverter.apply(
                termFactory.getDBCastFunctionalTerm(castingType, termFactory.getNullConstant()));
    }
}
