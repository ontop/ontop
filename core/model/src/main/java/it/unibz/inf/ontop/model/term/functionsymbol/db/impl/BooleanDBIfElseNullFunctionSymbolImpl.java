package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
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
    protected ImmutableTerm simplify(ImmutableExpression newCondition, ImmutableTerm newThenValue,
                                     TermFactory termFactory,
                                  VariableNullability variableNullability) {
        if (canBeReplacedByValue(newCondition, newThenValue, termFactory)) {
            return newThenValue;
        }
        if (newThenValue instanceof ImmutableExpression) {
            return termFactory.getImmutableExpression(this, newCondition, newThenValue);
        }
        else if (newThenValue instanceof DBConstant) {
            return newThenValue.equals(termFactory.getDBBooleanConstant(true))
                    // Produces a true when the condition is met, null otherwise
                    ? termFactory.getTrueOrNullFunctionalTerm(ImmutableList.of(newCondition))
                    // // Produces a false when the condition is met, null otherwise
                    : termFactory.getFalseOrNullFunctionalTerm(ImmutableList.of(
                            termFactory.getDBNot(newCondition)));
        }
        else if (newThenValue.isNull())
            return newThenValue;
        else
            throw new MinorOntopInternalBugException("Unexpected new \"then\" value for a boolean IF_ELSE_NULL: "
                    + newThenValue);
    }

    @Override
    public ImmutableTerm simplify2VL(ImmutableList<? extends ImmutableTerm> terms, TermFactory termFactory,
                                     VariableNullability variableNullability) {
        ImmutableTerm thenValue = terms.get(1);
        ImmutableExpression thenExpression = (thenValue instanceof ImmutableExpression)
                ? (ImmutableExpression) thenValue
                : termFactory.getIsTrue(Optional.of(thenValue)
                        .filter(v -> v instanceof NonFunctionalTerm)
                        .map(v -> (NonFunctionalTerm) v)
                        .orElseThrow(() -> new MinorOntopInternalBugException("Was an expected an immutable expression " +
                                "or non functional term as a then condition")));

        return termFactory.getConjunction((ImmutableExpression) terms.get(0), thenExpression)
                .simplify2VL(variableNullability);
    }
}
