package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBBooleanFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;

import javax.annotation.Nonnull;

public class MockupSimpleDBBooleanCastFunctionSymbol extends MockupSimpleDBCastFunctionSymbol
        implements DBBooleanFunctionSymbol {

    protected MockupSimpleDBBooleanCastFunctionSymbol(@Nonnull DBTermType inputBaseType, DBTermType targetType) {
        super(inputBaseType, targetType);
    }

    @Override
    public boolean blocksNegation() {
        return true;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        throw new UnsupportedOperationException();
    }
}
