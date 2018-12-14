package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;

import java.util.stream.Collectors;

/**
 * TODO: should it make it a non-DB boolean function symbol?
 *  --> that is, downgrading to a non-strict equality?
 */
public class DefaultDBStrictEqFunctionSymbol extends AbstractDBStrictEqNeqFunctionSymbol {
    private static String OPERATOR = " = ";
    private static String CONNECTOR = " AND ";

    protected DefaultDBStrictEqFunctionSymbol(int arity, TermType rootTermType, DBTermType dbBooleanTermType) {
        super("STRICT_EQ", arity, true, rootTermType, dbBooleanTermType);
    }

    @Override
    public String getNativeDBString(ImmutableList<String> termStrings) {
        if (termStrings.size() < 2)
            throw new IllegalArgumentException("At least two arguments were expected");
        String firstTerm = termStrings.get(0);
        String prefix = firstTerm + OPERATOR;

        return termStrings.stream()
                .skip(1)
                .map(s -> prefix + s)
                .collect(Collectors.joining(CONNECTOR));
    }
}
