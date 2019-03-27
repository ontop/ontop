package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * SQL-specific
 */
public class DefaultSQLSimpleDBCastFunctionSymbol extends AbstractSimpleDBCastFunctionSymbol {

    private final static String CAST_TEMPLATE = "CAST(%s AS %s)";


    protected DefaultSQLSimpleDBCastFunctionSymbol(@Nonnull DBTermType inputBaseType, DBTermType targetType) {
        super(inputBaseType, targetType);
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        if (terms.size() != getArity())
            throw new IllegalArgumentException(terms +
                    " does not respect the arity of " + getArity());

        return String.format(CAST_TEMPLATE, termConverter.apply(terms.get(0)), getTargetType().getCompleteName());
    }
}
