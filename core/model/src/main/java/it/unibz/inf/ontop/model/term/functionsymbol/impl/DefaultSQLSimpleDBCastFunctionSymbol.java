package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;

import javax.annotation.Nonnull;

/**
 * SQL-specific
 */
public class DefaultSQLSimpleDBCastFunctionSymbol extends AbstractSimpleDBCastFunctionSymbol {

    private final static String CAST_TEMPLATE = "CAST(%s AS %s)";


    protected DefaultSQLSimpleDBCastFunctionSymbol(@Nonnull DBTermType inputBaseType, DBTermType targetType) {
        super(inputBaseType, targetType);
    }

    @Override
    public boolean isInjective(ImmutableList<? extends ImmutableTerm> arguments, ImmutableSet<Variable> nonNullVariables) {
        return getInputType().isPresent();
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return getInputType().isPresent();
    }

    @Override
    public String getNativeDBString(ImmutableList<String> termStrings) {
        if (termStrings.size() != getArity())
            throw new IllegalArgumentException(termStrings +
                    " does not respect the arity of " + getArity());

        return String.format(CAST_TEMPLATE, termStrings.get(0), getTargetType().getName());
    }
}
