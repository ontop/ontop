package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolSerializer;
import it.unibz.inf.ontop.model.type.DBTermType;

public class DefaultDBCoalesceFunctionSymbol extends AbstractDBCoalesceFunctionSymbol {

    protected DefaultDBCoalesceFunctionSymbol(String nameInDialect, int arity, DBTermType rootDBTermType,
                                              DBFunctionSymbolSerializer serializer) {
        super(nameInDialect, arity, rootDBTermType, serializer);
    }

    @Override
    protected ImmutableFunctionalTerm createCoalesce(ImmutableList<ImmutableTerm> simplifiedTerms, TermFactory termFactory) {
        return termFactory.getDBCoalesce(simplifiedTerms);
    }
}
