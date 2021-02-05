package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.function.Function;

public abstract class AbstractDBStrBeforeOrAfterFunctionSymbol extends AbstractTypedDBFunctionSymbol {

    private final DBFunctionSymbolSerializer serializer;

    protected AbstractDBStrBeforeOrAfterFunctionSymbol(String name, DBTermType dbStringType, DBTermType rootDBType,
                                                       DBFunctionSymbolSerializer serializer) {
        super(name, ImmutableList.of(rootDBType, rootDBType), dbStringType);
        this.serializer = serializer;
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializer.getNativeDBString(terms, termConverter, termFactory);
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return false;
    }
}
