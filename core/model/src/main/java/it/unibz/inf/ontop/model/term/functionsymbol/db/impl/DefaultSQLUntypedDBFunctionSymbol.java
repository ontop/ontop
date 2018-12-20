package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nonnull;
import java.util.stream.IntStream;

public class DefaultSQLUntypedDBFunctionSymbol extends AbstractUntypedDBFunctionSymbol {

    protected DefaultSQLUntypedDBFunctionSymbol(@Nonnull String nameInDialect, int arity, DBTermType rootDBTermType) {
        super(nameInDialect, IntStream.range(0, arity)
                .boxed()
                .map(i -> (TermType) rootDBTermType)
                .collect(ImmutableCollectors.toList()));
    }
}
