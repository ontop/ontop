package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DefaultSQLCaseFunctionSymbol extends AbstractDBIfThenFunctionSymbol {

    private static final String WHEN_THEN_TEMPLATE = "    WHEN %s THEN %s\n";
    private static final String FULL_TEMPLATE = "CASE %s    ELSE %s \nEND";

    protected DefaultSQLCaseFunctionSymbol(int arity, DBTermType dbBooleanType, DBTermType rootDBTermType) {
        super("CASE"+arity, arity, dbBooleanType, rootDBTermType);
        if ((arity % 2 == 0) && (arity < 3))
            throw new IllegalArgumentException("A CASE function symbol must an odd arity >= 3");
    }

    @Override
    public String getNativeDBString(ImmutableList<String> termStrings) {
        String whenClauseString = IntStream.range(0, termStrings.size() / 2)
                .boxed()
                .map(i -> String.format(WHEN_THEN_TEMPLATE, termStrings.get(i), termStrings.get(i + 1)))
                .collect(Collectors.joining());

        return String.format(FULL_TEMPLATE, whenClauseString, termStrings.get(termStrings.size() - 1));
    }
}
