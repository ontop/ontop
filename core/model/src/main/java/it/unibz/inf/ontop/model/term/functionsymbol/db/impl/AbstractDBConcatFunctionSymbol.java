package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBConcatFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.function.Function;
import java.util.stream.IntStream;

public abstract class AbstractDBConcatFunctionSymbol extends AbstractTypedDBFunctionSymbol implements DBConcatFunctionSymbol {

    private final DBFunctionSymbolSerializer serializer;

    protected AbstractDBConcatFunctionSymbol(String nameInDialect, int arity, DBTermType dbStringType,
                                             DBTermType rootDBTermType, DBFunctionSymbolSerializer serializer) {
        super(nameInDialect + arity,
                // No restriction on the input types TODO: check if OK
                IntStream.range(0, arity)
                        .boxed()
                        .map(i -> (TermType) rootDBTermType)
                        .collect(ImmutableCollectors.toList()), dbStringType);
        this.serializer = serializer;
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializer.getNativeDBString(terms, termConverter, termFactory);
    }
}
