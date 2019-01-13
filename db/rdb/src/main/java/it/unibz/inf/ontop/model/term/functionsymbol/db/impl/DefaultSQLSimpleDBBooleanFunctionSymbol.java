package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBBooleanFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;

public class DefaultSQLSimpleDBBooleanFunctionSymbol extends AbstractSimpleTypedDBFunctionSymbol
        implements DBBooleanFunctionSymbol {


    protected DefaultSQLSimpleDBBooleanFunctionSymbol(String nameInDialect, int arity, DBTermType dbBooleanType,
                                                      boolean isInjective, DBTermType rootDBTermType) {
        super(nameInDialect, arity, dbBooleanType, isInjective, rootDBTermType);
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
