package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.function.Function;

public class DefaultSQLDBIsNullOrNotFunctionSymbol extends AbstractDBIsNullOrNotFunctionSymbol {

    private final String template;

    protected DefaultSQLDBIsNullOrNotFunctionSymbol(boolean isNull, DBTermType dbBooleanTermType, DBTermType rootDBTermType) {
        super(isNull, dbBooleanTermType, rootDBTermType);
        this.template = isNull
                ?  "%s IS NULL"
                :  "%s IS NOT NULL";
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format(template, termConverter.apply(terms.get(0)));
    }
}
