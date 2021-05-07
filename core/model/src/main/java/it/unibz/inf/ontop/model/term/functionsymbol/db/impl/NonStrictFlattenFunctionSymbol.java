package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.function.Function;

public class NonStrictFlattenFunctionSymbol extends AbstractFlattenFunctionSymbol {

    protected NonStrictFlattenFunctionSymbol(DBTermType rootDBType, DBTermType targetType) {
        super("DB_FLATTEN", rootDBType, targetType);
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
    }
}
