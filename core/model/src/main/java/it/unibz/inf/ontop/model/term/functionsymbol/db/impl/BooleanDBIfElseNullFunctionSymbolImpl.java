package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.IncrementalEvaluation;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBBooleanFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Optional;

public class BooleanDBIfElseNullFunctionSymbolImpl extends DefaultDBIfElseNullFunctionSymbol
        implements DBBooleanFunctionSymbol {

    protected BooleanDBIfElseNullFunctionSymbolImpl(DBTermType dbBooleanType) {
        super("BOOL_IF_ELSE_NULL", dbBooleanType, dbBooleanType);
    }

    @Override
    public boolean blocksNegation() {
        return false;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        ImmutableExpression thenCondition = Optional.of(subTerms.get(1))
                .filter(t -> t instanceof ImmutableExpression)
                .map(t -> termFactory.getDBNot((ImmutableExpression) t))
                .orElseThrow(() -> new MinorOntopInternalBugException("BooleanDBIfElseNullFunctionSymbol was " +
                        "expecting its second sub-term to be an expression"));

        return termFactory.getImmutableExpression(this, subTerms.get(0), thenCondition);
    }

    @Override
    public ImmutableTerm simplify(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory,
                                  VariableNullability variableNullability) {
        if (terms.stream().anyMatch(t -> !(t instanceof ImmutableExpression))) {
            throw new MinorOntopInternalBugException("BooleanDBIfElseNullFunctionSymbol was expecting " +
                    "only ImmutableExpression as sub-terms");
        }

        // TODO: evaluate in 2-value logic
        IncrementalEvaluation conditionEvaluation =
                ((ImmutableExpression) terms.get(0)).evaluate(variableNullability, true);
        switch (conditionEvaluation.getStatus()) {
            case SAME_EXPRESSION:
                throw new MinorOntopInternalBugException("Should not be the same expression");
            case SIMPLIFIED_EXPRESSION:
                ImmutableExpression newCondition = conditionEvaluation.getNewExpression().get();
                return simplify(newCondition, (ImmutableExpression) terms.get(1), termFactory, variableNullability);
            case IS_NULL:
            case IS_FALSE:
                return termFactory.getNullConstant();
            case IS_TRUE:
            default:
                return terms.get(1).simplify(variableNullability);
        }
    }

    protected ImmutableTerm simplify(ImmutableExpression newCondition, ImmutableExpression thenExpression,
                                     TermFactory termFactory,
                                  VariableNullability variableNullability) {

        IncrementalEvaluation thenEvaluation = thenExpression.evaluate(variableNullability, true);
        switch (thenEvaluation.getStatus()) {
            case SAME_EXPRESSION:
                throw new MinorOntopInternalBugException("Should not be the same expression");
            case SIMPLIFIED_EXPRESSION:
                ImmutableExpression newThenExpression = thenEvaluation.getNewExpression().get();
                // TODO: check if we are not in the case of IF_ELSE_NULL(IS_NOT_NULL(x),IS_TRUE(x)) ?
                return termFactory.getImmutableExpression(this, newCondition, newThenExpression);
            case IS_NULL:
                return termFactory.getNullConstant();
            case IS_FALSE:
                return newCondition.negate(termFactory);
            case IS_TRUE:
            default:
                return newCondition;
        }
    }
}
