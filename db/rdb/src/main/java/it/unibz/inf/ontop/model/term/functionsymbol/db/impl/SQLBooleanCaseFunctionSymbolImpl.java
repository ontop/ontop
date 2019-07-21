package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBBooleanFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Map;
import java.util.stream.Stream;

public class SQLBooleanCaseFunctionSymbolImpl extends DefaultSQLCaseFunctionSymbol implements DBBooleanFunctionSymbol {
    protected SQLBooleanCaseFunctionSymbolImpl(int arity, DBTermType dbBooleanType, DBTermType rootDBTermType) {
        super("BOOL_CASE" + arity, arity, dbBooleanType, rootDBTermType);
    }

    @Override
    public boolean blocksNegation() {
        return true;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected ImmutableExpression simplifyValue(ImmutableTerm immutableTerm, VariableNullability variableNullability,
                                                TermFactory termFactory) {
        ImmutableTerm simplifiedTerm = immutableTerm.simplify(variableNullability);
        return (simplifiedTerm instanceof ImmutableExpression)
                ? (ImmutableExpression) simplifiedTerm
                : termFactory.getIsTrue((NonFunctionalTerm) simplifiedTerm);
    }

    @Override
    protected ImmutableFunctionalTerm buildCase(Stream<Map.Entry<ImmutableExpression, ? extends ImmutableTerm>> newWhenPairs,
                                                ImmutableTerm defaultValue, TermFactory termFactory) {
        return termFactory.getDBBooleanCase(
                (Stream<Map.Entry<ImmutableExpression, ImmutableExpression>>)(Stream<?>) newWhenPairs,
                (ImmutableExpression) defaultValue);
    }
}
