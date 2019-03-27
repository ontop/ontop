package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;

import java.util.function.Function;

public class DBFunctionSymbolWithSerializerImpl extends AbstractTypedDBFunctionSymbol {
    private final boolean isAlwaysInjective;
    private final DBFunctionSymbolSerializer serializer;

    protected DBFunctionSymbolWithSerializerImpl(String name, ImmutableList<TermType> inputDBTypes,
                                                 DBTermType targetType,
                                                 boolean isAlwaysInjective,
                                                 DBFunctionSymbolSerializer serializer) {
        super(name, inputDBTypes, targetType);
        this.isAlwaysInjective = isAlwaysInjective;
        this.serializer = serializer;
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter,
                                    TermFactory termFactory) {
        return serializer.getNativeDBString(terms, termConverter, termFactory);
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return isAlwaysInjective;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }
}
