package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.stream.IntStream;

/**
 * For AND, OR, etc.
 */
public abstract class AbstractDBBooleanConnectorFunctionSymbol extends DBBooleanFunctionSymbolImpl {

    protected AbstractDBBooleanConnectorFunctionSymbol(String name, int arity, DBTermType dbBooleanTermType) {
        super(name + arity,
                IntStream.range(0, arity)
                        .boxed()
                        .map(i -> dbBooleanTermType)
                        .collect(ImmutableCollectors.toList()),
                dbBooleanTermType);
    }
}
