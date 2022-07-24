package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Here we don't know how to post-process functions
 *
 * Different types are associated to the same "nameInDialect", so it is important to include
 * the type in the function symbol name
 *
 * Same type as input and as output
 *
 */
public abstract class AbstractSimpleMultitypedDBFunctionSymbol extends AbstractTypedDBFunctionSymbol {

    private static final String FUNCTIONAL_TEMPLATE = "%s(%s)";

    private final String nameInDialect;
    private final boolean isInjective;

    protected AbstractSimpleMultitypedDBFunctionSymbol(String nameInDialect, int arity, DBTermType targetType, boolean isInjective) {
        super(nameInDialect + targetType + arity, IntStream.range(0, arity)
                    .mapToObj(i -> (TermType) targetType)
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
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter,
                                    TermFactory termFactory) {
        return String.format(FUNCTIONAL_TEMPLATE, nameInDialect,
                terms.stream()
                        .map(termConverter::apply)
                        .collect(Collectors.joining(",")));
    }
}
