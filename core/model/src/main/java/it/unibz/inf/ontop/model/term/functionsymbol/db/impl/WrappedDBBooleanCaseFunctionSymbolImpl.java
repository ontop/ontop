package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBIsTrueFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class WrappedDBBooleanCaseFunctionSymbolImpl extends DBBooleanCaseFunctionSymbolImpl {

    protected WrappedDBBooleanCaseFunctionSymbolImpl(int arity, DBTermType dbBooleanType,
                                                     DBTermType rootDBTermType, boolean doOrderingMatter) {
        super(arity, dbBooleanType, rootDBTermType, doOrderingMatter);
    }

    /**
     * Replaces the then expressions by cases. Wraps the full expression into an equality.
     */
    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {

        ImmutableList<ImmutableTerm> newTerms = IntStream.range(0, terms.size())
                .mapToObj(i -> (i % 2 == 0) && (i < terms.size() - 1)
                        ? terms.get(i)
                        : wrapThenExpression((ImmutableExpression) terms.get(i), termFactory))
                .collect(ImmutableCollectors.toList());

        String caseString = super.getNativeDBString(newTerms, termConverter, termFactory);
        return String.format("(%s = %s)",
                caseString,
                termConverter.apply(termFactory.getDBBooleanConstant(true)));
    }

    private ImmutableTerm wrapThenExpression(ImmutableExpression thenExpression, TermFactory termFactory) {
        if (thenExpression.getFunctionSymbol() instanceof DBIsTrueFunctionSymbol)
            return thenExpression.getTerm(0);

        return termFactory.getDBCaseElseNull(Stream.of(
                Maps.immutableEntry(thenExpression, termFactory.getDBBooleanConstant(true)),
                Maps.immutableEntry(termFactory.getDBNot(thenExpression), termFactory.getDBBooleanConstant(false))
        ), false);
    }
}
