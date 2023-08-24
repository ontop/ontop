package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.evaluator.QueryContext;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

import javax.annotation.Nullable;

public class OntopContainsRoleOrGroupFunctionSymbol extends AbstractBooleanAuthorizationFunctionSymbol {

    public static final String ONTOP_CONTAINS_ROLE_OR_GROUP = "ONTOP_CONTAINS_ROLE_OR_GROUP";
    protected OntopContainsRoleOrGroupFunctionSymbol(DBTermType dbStringType, DBTermType dbBooleanTermType) {
        super(ONTOP_CONTAINS_ROLE_OR_GROUP, ImmutableList.of(dbStringType), dbBooleanTermType);
    }

    @Override
    public ImmutableExpression simplifyWithContext(ImmutableList<ImmutableTerm> terms, @Nullable QueryContext queryContext,
                                                   TermFactory termFactory) {
        var subTerm = terms.get(0);

        if (queryContext == null)
            return termFactory.getStrictEquality(termFactory.getNullConstant(), termFactory.getNullConstant());

        return termFactory.getDisjunction(
                queryContext.getRolesOrGroups().stream()
                        .map(r -> termFactory.getStrictEquality(termFactory.getDBStringConstant(r), subTerm)))
                .orElseGet(() -> termFactory.getFalseOrNullFunctionalTerm(ImmutableList.of(termFactory.getDBIsNull(subTerm))));
    }
}
