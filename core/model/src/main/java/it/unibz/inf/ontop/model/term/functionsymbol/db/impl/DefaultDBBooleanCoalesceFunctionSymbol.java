package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBBooleanFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import java.util.Optional;

public class DefaultDBBooleanCoalesceFunctionSymbol extends AbstractDBCoalesceFunctionSymbol implements DBBooleanFunctionSymbol {

    private final DBTermType dbBooleanTermType;

    protected DefaultDBBooleanCoalesceFunctionSymbol(String nameInDialect, int arity, DBTermType rootDBTermType, DBTermType dbBooleanTermType,
                                                     DBFunctionSymbolSerializer serializer) {
        super(nameInDialect, arity, rootDBTermType, serializer);
        this.dbBooleanTermType = dbBooleanTermType;
    }

    @Override
    public boolean blocksNegation() {
        return true;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        throw new UnsupportedOperationException("Should have not been called");
    }

    @Override
    public Optional<TermTypeInference> inferType(ImmutableList<? extends ImmutableTerm> terms) {
        return Optional.of(TermTypeInference.declareTermType(dbBooleanTermType));
    }

    @Override
    protected ImmutableFunctionalTerm createCoalesce(ImmutableList<ImmutableTerm> simplifiedTerms, TermFactory termFactory) {
        return termFactory.getDBBooleanCoalesce(simplifiedTerms);
    }
}
