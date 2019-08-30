package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBBooleanFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.stream.Stream;

public class DBBooleanCaseFunctionSymbolImpl extends DefaultDBCaseFunctionSymbol implements DBBooleanFunctionSymbol {
    protected DBBooleanCaseFunctionSymbolImpl(int arity, DBTermType dbBooleanType, DBTermType rootDBTermType,
                                              boolean doOrderingMatter) {
        super("BOOL_CASE" + arity, arity, dbBooleanType, rootDBTermType, doOrderingMatter);
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
                (ImmutableExpression) defaultValue, doOrderingMatter);
    }

    @Override
    public ImmutableTerm simplify2VL(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory,
                                     VariableNullability variableNullability) {
        if (!terms.stream().allMatch(t -> t instanceof ImmutableExpression))
            throw new MinorOntopInternalBugException("Was expecting all its arguments to be ImmutableExpression-s");

        ImmutableList<ImmutableExpression> twoVLExpressions = terms.stream()
                .map(t -> (ImmutableExpression) t)
                .map(t -> t.simplify2VL(variableNullability))
                .map(t -> (t instanceof ImmutableExpression)
                        ? (ImmutableExpression) t
                        : termFactory.getIsTrue((NonFunctionalTerm) t))
                .collect(ImmutableCollectors.toList());

        ImmutableTerm newTerm = simplify(twoVLExpressions, termFactory, variableNullability);
        // Makes sure that the returned expression has been inform that "2VL simplifications" can be applied
        // Prevents an infinite loop
        if (newTerm instanceof ImmutableExpression) {
            ImmutableExpression newExpression = (ImmutableExpression) newTerm;
            if ((!this.equals(newExpression.getFunctionSymbol())) || (!terms.equals(newExpression.getTerms()))) {
                return newExpression.simplify2VL(variableNullability);
            }
        }
        if (newTerm.isNull())
            return termFactory.getDBBooleanConstant(false);
        return newTerm;
    }
}
