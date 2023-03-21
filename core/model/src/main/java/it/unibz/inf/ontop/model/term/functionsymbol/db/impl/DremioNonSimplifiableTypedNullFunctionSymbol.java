package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

public class DremioNonSimplifiableTypedNullFunctionSymbol extends NonSimplifiableTypedNullFunctionSymbol {

    protected DremioNonSimplifiableTypedNullFunctionSymbol(DBTermType targetType) {
        super(targetType);
    }

    protected DremioNonSimplifiableTypedNullFunctionSymbol(DBTermType targetType, DBTermType castingType) {
        super(targetType, castingType);
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        var comparison = termFactory.getDBNumericInequality(
                InequalityLabel.GT,
                termFactory.getDBRand(UUID.randomUUID()),
                termFactory.getDBIntegerConstant(1));


        var constant = castingType.getCategory() == DBTermType.Category.BOOLEAN ? termFactory.getDBBooleanConstant(false) :
                termFactory.getDBCastFunctionalTerm(castingType, termFactory.getDBRand(UUID.randomUUID()));

        var caseTerm = termFactory.getDBCaseElseNull(
                Stream.of(Maps.immutableEntry(comparison, constant)),
                false
        );
        return termConverter.apply(
                termFactory.getDBCastFunctionalTerm(castingType, caseTerm));
    }
}
