package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.functionsymbol.DBBooleanFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.IntStream;

public abstract class DBBooleanFunctionSymbolImpl extends BooleanFunctionSymbolImpl implements DBBooleanFunctionSymbol {

    private final String nameInDialect;
    private static final String FUNCTIONAL_TEMPLATE = "%s(%s)";

    protected DBBooleanFunctionSymbolImpl(String nameInDialect, int arity, DBTermType dbBooleanTermType) {
        super(nameInDialect + arity,
                IntStream.range(0, arity)
                        .boxed()
                        .map(i -> dbBooleanTermType)
                        .collect(ImmutableCollectors.toList()),
                dbBooleanTermType);
        this.nameInDialect = nameInDialect;
    }

    @Override
    public String getNativeDBString(ImmutableList<String> termStrings) {
        return String.format(FUNCTIONAL_TEMPLATE, nameInDialect,
                String.join( ",", termStrings));
    }

    protected String inBrackets(String expression) {
        return "(" + expression + ")";
    }
}
