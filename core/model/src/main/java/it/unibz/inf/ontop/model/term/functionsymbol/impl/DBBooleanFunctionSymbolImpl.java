package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.functionsymbol.DBBooleanFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.IntStream;

public abstract class DBBooleanFunctionSymbolImpl extends BooleanFunctionSymbolImpl implements DBBooleanFunctionSymbol {

    protected DBBooleanFunctionSymbolImpl(String name, int arity, DBTermType dbBooleanTermType) {
        super(name + arity,
                IntStream.range(0, arity)
                        .boxed()
                        .map(i -> dbBooleanTermType)
                        .collect(ImmutableCollectors.toList()),
                dbBooleanTermType);
    }

    protected String inBrackets(String expression) {
        return "(" + expression + ")";
    }
}
