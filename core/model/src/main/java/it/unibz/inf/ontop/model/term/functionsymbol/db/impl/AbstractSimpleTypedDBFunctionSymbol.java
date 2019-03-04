package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Here we don't know the input types and how to post-process functions
 *
 */
public abstract class AbstractSimpleTypedDBFunctionSymbol extends AbstractTypedDBFunctionSymbol {

    private static final String FUNCTIONAL_TEMPLATE = "%s(%s)";

    private final String nameInDialect;
    private final boolean isInjective;

    protected AbstractSimpleTypedDBFunctionSymbol(String nameInDialect, int arity, DBTermType targetType, boolean isInjective,
                                                  DBTermType rootDBTermType) {
        super(nameInDialect + arity, IntStream.range(0, arity)
                    .boxed()
                    .map(i -> (TermType) rootDBTermType)
                    .collect(ImmutableCollectors.toList()),
                targetType);
        this.nameInDialect = nameInDialect;
        this.isInjective = isInjective;
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
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format(FUNCTIONAL_TEMPLATE, nameInDialect,
                terms.stream()
                        .map(termConverter::apply)
                        .collect(Collectors.joining(",")));
    }
}
