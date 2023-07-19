package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.BooleanAuthorizationFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;

import javax.annotation.Nonnull;

public abstract class AbstractBooleanAuthorizationFunctionSymbol extends AbstractDBAuthorizationFunctionSymbol
        implements BooleanAuthorizationFunctionSymbol {
    protected AbstractBooleanAuthorizationFunctionSymbol(@Nonnull String name, ImmutableList<TermType> expectedBaseTypes,
                                                         DBTermType dbBooleanTermType) {
        super(name, expectedBaseTypes, dbBooleanTermType);
    }

    @Override
    public boolean blocksNegation() {
        return true;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        throw new IllegalArgumentException("Negation is not supported by this function symbol");
    }
}
