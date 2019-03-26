package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.function.Function;

public class SQLServerCurrentTimestampFunctionSymbol extends DefaultSQLSimpleTypedDBFunctionSymbol {
    private static final String STR = "CURRENT_TIMESTAMP";

    protected SQLServerCurrentTimestampFunctionSymbol(DBTermType dbDatetimeType, DBTermType rootDBTermType) {
        super(STR, 0, dbDatetimeType, true, rootDBTermType);
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return STR;
    }
}
