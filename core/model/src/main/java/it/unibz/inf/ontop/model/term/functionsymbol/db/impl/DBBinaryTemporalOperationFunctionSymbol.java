package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.function.Function;

public class DBBinaryTemporalOperationFunctionSymbol extends AbstractTypedDBFunctionSymbol {

    protected final String template;
    protected final DBTermType resultType;

    protected DBBinaryTemporalOperationFunctionSymbol(String operator, DBTermType dbType1, DBTermType dbType2,
                                                      DBTermType resultType) {
        super(String.format("%s_%s_%s", dbType1, dbType2, operator), ImmutableList.of(dbType1, dbType2), resultType);
        this.resultType = resultType;
        this.template = "%s " + operator + " %s";
    }

    @Override
    protected boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format(template, termConverter.apply(terms.get(0)), termConverter.apply(terms.get(1)));
    }
}
