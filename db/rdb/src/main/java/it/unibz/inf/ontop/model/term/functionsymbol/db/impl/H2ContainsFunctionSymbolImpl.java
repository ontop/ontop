package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.function.Function;

public class H2ContainsFunctionSymbolImpl extends AbstractDBContainsFunctionSymbol {

    protected H2ContainsFunctionSymbolImpl(DBTermType abstractRootTermType, DBTermType dbBooleanTermType) {
        super(abstractRootTermType, dbBooleanTermType);
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return inBrackets(String.format("POSITION(%s,%s) > 0",
                termConverter.apply(terms.get(1)),
                termConverter.apply(terms.get(0))));
    }
}
