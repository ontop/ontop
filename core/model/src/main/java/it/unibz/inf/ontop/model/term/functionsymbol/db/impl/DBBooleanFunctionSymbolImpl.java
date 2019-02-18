package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBBooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.BooleanFunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;

import javax.annotation.Nonnull;

public abstract class DBBooleanFunctionSymbolImpl extends BooleanFunctionSymbolImpl implements DBBooleanFunctionSymbol {


    protected DBBooleanFunctionSymbolImpl(@Nonnull String name, @Nonnull ImmutableList<TermType> expectedBaseTypes,
                                          DBTermType dbBooleanTermType) {
        super(name, expectedBaseTypes, dbBooleanTermType);
    }

    protected String inBrackets(String expression) {
        return "(" + expression + ")";
    }

    @Override
    protected boolean mayReturnNullWithoutNullArguments() {
        return false;
    }

    /**
     * By default, to be overridden when necessary
     */
    @Override
    public boolean isPreferringToBePostProcessedOverBeingBlocked() {
        return false;
    }
}
