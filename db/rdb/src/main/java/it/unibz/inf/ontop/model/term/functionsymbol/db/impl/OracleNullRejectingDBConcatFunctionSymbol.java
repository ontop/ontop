package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;


public class OracleNullRejectingDBConcatFunctionSymbol extends NullRejectingDBConcatFunctionSymbol {
    protected OracleNullRejectingDBConcatFunctionSymbol(int arity, DBTermType dbStringType,
                                                        DBTermType rootDBTermType) {
        super("NR_CONCAT", arity, dbStringType, rootDBTermType,
                (terms, termConverter, termFactory) -> {
                    throw new UnsupportedOperationException("Should have been simplified");
                });
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms, TermFactory termFactory,
                                                     VariableNullability variableNullability) {
        ImmutableExpression condition = termFactory.getDBIsNotNull(newTerms.stream())
                .get();

        ImmutableFunctionalTerm thenValue = termFactory.getImmutableFunctionalTerm(
                termFactory.getDBFunctionSymbolFactory().getDBConcatOperator(getArity()), newTerms);

        return termFactory.getIfElseNull(condition, thenValue)
                .simplify(variableNullability);
    }
}
