package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBStrictEqFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;

import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * TODO: should it make it a non-DB boolean function symbol?
 * {@code --> } that is, downgrading to a non-strict equality?
 */
public class DefaultDBStrictEqFunctionSymbol extends AbstractDBStrictEqNeqFunctionSymbol implements DBStrictEqFunctionSymbol {
    private static String OPERATOR = " = ";
    private static String CONNECTOR = " AND ";

    protected DefaultDBStrictEqFunctionSymbol(int arity, TermType rootTermType, DBTermType dbBooleanTermType) {
        super("STRICT_EQ", arity, true, rootTermType, dbBooleanTermType);
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        if (terms.size() < 2)
            throw new IllegalArgumentException("At least two arguments were expected");
        String firstTerm = termConverter.apply(terms.get(0));
        String prefix = firstTerm + OPERATOR;

        return terms.stream()
                .skip(1)
                .map(termConverter::apply)
                .map(s -> prefix + s)
                .collect(Collectors.joining(CONNECTOR));
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        return termFactory.getStrictNEquality(subTerms);
    }
}
