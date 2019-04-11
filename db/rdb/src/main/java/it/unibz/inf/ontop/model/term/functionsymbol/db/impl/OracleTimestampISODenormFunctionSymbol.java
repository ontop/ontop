package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.function.Function;


public class OracleTimestampISODenormFunctionSymbol extends AbstractTimestampISODenormFunctionSymbol {

    protected static final String TEMPLATE = "TO_TIMESTAMP_TZ(%s, 'YYYY-MM-DD HH24:MI:SSxFF TZH:TZM')";

    protected OracleTimestampISODenormFunctionSymbol(DBTermType timestampWithTZType, DBTermType dbStringType) {
        super(timestampWithTZType, dbStringType);
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        ImmutableFunctionalTerm replaceTTerm = termFactory.getDBReplace(terms.get(0),
                termFactory.getDBStringConstant("T"),
                termFactory.getDBStringConstant(" "));

        ImmutableFunctionalTerm newTerm = termFactory.getDBReplace(replaceTTerm,
                termFactory.getDBStringConstant("Z"),
                termFactory.getDBStringConstant("+00:00"));

        return String.format(TEMPLATE, termConverter.apply(newTerm));
    }
}
