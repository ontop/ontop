package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBBooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;

import javax.annotation.Nonnull;

public class DefaultSimpleDBBooleanCastFunctionSymbol extends DefaultSimpleDBCastFunctionSymbol
        implements DBBooleanFunctionSymbol {

    protected DefaultSimpleDBBooleanCastFunctionSymbol(@Nonnull DBTermType inputBaseType, DBTermType booleanType,
                                                       DBFunctionSymbolSerializer serializer) {
        super(inputBaseType, booleanType, serializer);
    }

    @Override
    public boolean blocksNegation() {
        return true;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        throw new UnsupportedOperationException("DefaultSQLSimpleDBBooleanCastFunctionSymbol blocks negation");
    }
}
