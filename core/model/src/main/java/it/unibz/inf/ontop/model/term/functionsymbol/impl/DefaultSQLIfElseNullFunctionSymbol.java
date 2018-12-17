package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.function.Function;

public class DefaultSQLIfElseNullFunctionSymbol extends AbstractDBIfElseNullFunctionSymbol {

    private static final String TEMPLATE = "CASE WHEN %s THEN %s ELSE NULL END";

    protected DefaultSQLIfElseNullFunctionSymbol(DBTermType dbBooleanType, DBTermType rootDBTermType) {
        super(dbBooleanType, rootDBTermType);
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format(
                TEMPLATE,
                terms.stream()
                        .map(termConverter::apply)
                        .toArray());
    }
}
