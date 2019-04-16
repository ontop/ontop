package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;

import java.util.function.Function;

/**
 * In SQL SERVER, round() is binary or ternary, not unary.
 */
public class SQLServerRoundFunctionSymbol extends AbstractTypedDBFunctionSymbol {
    private static final String TEMPLATE = "ROUND(%s,0)";

    protected SQLServerRoundFunctionSymbol(DBTermType mainNumericType) {
        super("ROUND" + mainNumericType + "1", ImmutableList.of(mainNumericType), mainNumericType);
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format(TEMPLATE, termConverter.apply(terms.get(0)));
    }
}
