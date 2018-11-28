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

    protected DefaultSQLSimpleDBCastFunctionSymbol(@Nonnull DBTermType inputBaseType, DBTermType targetType) {
        super(inputBaseType, targetType);
    }

    @Override
    public boolean isInjective(ImmutableList<? extends ImmutableTerm> arguments, ImmutableSet<Variable> nonNullVariables) {
        return getInputType().isPresent();
    }

    @Override
    public boolean canBePostProcessed() {
        return getInputType().isPresent();
    }
}
