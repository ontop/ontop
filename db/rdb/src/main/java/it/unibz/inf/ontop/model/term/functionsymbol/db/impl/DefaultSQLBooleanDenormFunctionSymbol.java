package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.function.Function;

/**
 * SQL-specific
 */
public class DefaultSQLBooleanDenormFunctionSymbol extends AbstractBooleanDenormFunctionSymbol {

    protected DefaultSQLBooleanDenormFunctionSymbol(DBTermType booleanType, DBTermType stringType) {
        super(booleanType, stringType);
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new RuntimeException("TODO: implement getNativeDBString for " + getClass());
    }
}
