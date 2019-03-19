package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIsTrueFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Optional;
import java.util.function.Function;

public class SQLServerBooleanDBIfElseNullFunctionSymbolImpl extends BooleanDBIfElseNullFunctionSymbolImpl {

    private static final String TEMPLATE = "IIF(%s,%s,NULL) = 1";

    protected SQLServerBooleanDBIfElseNullFunctionSymbolImpl(DBTermType dbBooleanType) {
        super(dbBooleanType);
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        return String.format(TEMPLATE,
                termConverter.apply(terms.get(0)),
                termConverter.apply(unwrapIsTrue(terms.get(1))));
    }

    /**
     * Equalities are not supported by this dialect as a valid second argument of IIF, so IS_TRUE is unwrap.
     */
    private ImmutableTerm unwrapIsTrue(ImmutableTerm term) {
        return Optional.of(term)
                .filter(t -> t instanceof ImmutableFunctionalTerm)
                .map(t -> (ImmutableFunctionalTerm) t)
                .filter(t -> t.getFunctionSymbol() instanceof DBIsTrueFunctionSymbol)
                .map(t -> t.getTerm(0))
                .orElse(term);
    }
}
