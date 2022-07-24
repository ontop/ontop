package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Here we don't know the input types and how to post-process functions
 *
 */
public class SimpleTypedDBFunctionSymbolImpl extends AbstractTypedDBFunctionSymbol {

    private final boolean isInjective;
    private final DBFunctionSymbolSerializer serializer;

    protected SimpleTypedDBFunctionSymbolImpl(String nameWithoutArity, int arity, DBTermType targetType, boolean isInjective,
                                              DBTermType rootDBTermType,
                                              DBFunctionSymbolSerializer serializer) {
        super(nameWithoutArity + arity, IntStream.range(0, arity)
                        .mapToObj(i -> (TermType) rootDBTermType)
                        .collect(ImmutableCollectors.toList()),
                targetType);
        this.isInjective = isInjective;
        this.serializer = serializer;
    }

    @Override
    public boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
        return isInjective;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return false;
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return serializer.getNativeDBString(terms, termConverter, termFactory);
    }
}
