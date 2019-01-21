package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.function.Function;

public class DBHashFunctionSymbolImpl extends AbstractTypedDBFunctionSymbol {
    private final DBFunctionSymbolSerializer serializer;

    protected DBHashFunctionSymbolImpl(String name, DBTermType rootDBType, DBTermType dbStringType,
                                       DBFunctionSymbolSerializer serializer) {
        super(name, ImmutableList.of(rootDBType), dbStringType);
        this.serializer = serializer;
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter,
                                    TermFactory termFactory) {
        return serializer.getNativeDBString(terms, termConverter, termFactory);
    }

    /**
     * False because collision are theoretically possible
     */
    @Override
    protected boolean isAlwaysInjective() {
        return false;
    }

    /**
     * The class would have to be made abstract for supporting post-processing
     */
    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }
}
