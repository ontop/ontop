package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBBooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;

public class DBBooleanFunctionSymbolWithSerializerImpl extends DBFunctionSymbolWithSerializerImpl
        implements DBBooleanFunctionSymbol {

    protected DBBooleanFunctionSymbolWithSerializerImpl(String name, ImmutableList<TermType> inputDBTypes,
                                                        DBTermType dbBooleanType, boolean isAlwaysInjective,
                                                        DBFunctionSymbolSerializer serializer) {
        super(name, inputDBTypes, dbBooleanType, isAlwaysInjective, serializer);
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
