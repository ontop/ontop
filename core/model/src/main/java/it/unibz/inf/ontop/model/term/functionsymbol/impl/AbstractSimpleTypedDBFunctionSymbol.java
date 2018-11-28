package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.IntStream;

/**
 * Here we don't know the input types and how to post-process functions
 *
 */
public class AbstractSimpleTypedDBFunctionSymbol extends AbstractTypedDBFunctionSymbol {

    private final boolean isInjective;

    protected AbstractSimpleTypedDBFunctionSymbol(String nameInDialect, int arity, DBTermType targetType, boolean isInjective,
                                                  DBTermType rootDBTermType) {
        super(nameInDialect + arity, IntStream.range(0, arity)
                    .boxed()
                    .map(i -> (TermType) rootDBTermType)
                    .collect(ImmutableCollectors.toList()),
                targetType);
        this.isInjective = isInjective;
    }

    @Override
    public boolean isInjective(ImmutableList<? extends ImmutableTerm> arguments, ImmutableSet<Variable> nonNullVariables) {
        return isInjective;
    }

    @Override
    public boolean canBePostProcessed() {
        return false;
    }
}
