package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.evaluator.QueryContext;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

import javax.annotation.Nullable;

public class OntopUserFunctionSymbolImpl extends AbstractDBAuthorizationFunctionSymbol {

    public static final String ONTOP_USER = "ONTOP_USER";

    protected OntopUserFunctionSymbolImpl(DBTermType dbBooleanTermType) {
        super(ONTOP_USER, ImmutableList.of(),dbBooleanTermType);
    }

    @Override
    public ImmutableTerm simplifyWithContext(ImmutableList<ImmutableTerm> terms, @Nullable QueryContext queryContext,
                                        TermFactory termFactory) {
        if (queryContext == null)
            return termFactory.getNullConstant();

        return queryContext.getUsername()
                .map(termFactory::getDBStringConstant)
                .map(c -> (Constant) c)
                .orElseGet(termFactory::getNullConstant);
    }
}
