package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;

import java.util.function.Function;

public class MockupDBIsNullOrNotFunctionSymbolImpl extends AbstractDBIsNullOrNotFunctionSymbol {

    protected MockupDBIsNullOrNotFunctionSymbolImpl(boolean isNull, DBTermType dbBooleanTermType, DBTermType rootDBTermType) {
        super(isNull, dbBooleanTermType, rootDBTermType);
    }

    @Override
    public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms,
                                    Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
        throw new UnsupportedOperationException("getNativeDBString is not supported by the mockup implementation");
    }
}
