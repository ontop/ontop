package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.TermType;

public abstract class AbstractDBIsNullOrNotFunctionSymbol extends DBBooleanFunctionSymbolImpl {

    private static final String IS_NULL_NAME = "IS_NULL";
    private static final String IS_NOT_NULL_NAME = "IS_NOT_NULL";

    private final boolean isNull;

    protected AbstractDBIsNullOrNotFunctionSymbol(boolean isNull, DBTermType dbBooleanTermType, DBTermType rootDBTermType) {
        super(isNull ? IS_NULL_NAME : IS_NOT_NULL_NAME, ImmutableList.of(rootDBTermType), dbBooleanTermType);
        this.isNull = isNull;
    }

    @Override
    public boolean blocksNegation() {
        return false;
    }

    @Override
    public ImmutableExpression negate(ImmutableList<? extends ImmutableTerm> subTerms, TermFactory termFactory) {
        ImmutableTerm subTerm = subTerms.get(0);
        return isNull
                ? termFactory.getDBIsNotNull(subTerm)
                : termFactory.getDBIsNull(subTerm);
    }

    @Override
    protected boolean isAlwaysInjective() {
        return false;
    }

    @Override
    public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
        return true;
    }

    @Override
    protected ImmutableTerm buildTermAfterEvaluation(ImmutableList<ImmutableTerm> newTerms,
                                                     boolean isInConstructionNodeInOptimizationPhase,
                                                     TermFactory termFactory, VariableNullability variableNullability) {
        ImmutableTerm newTerm = newTerms.get(0);
        if (newTerm instanceof Constant) {
            return termFactory.getDBBooleanConstant(isNull == newTerm.isNull());
        }
        else if (newTerm instanceof Variable) {
            return variableNullability.isPossiblyNullable((Variable) newTerm)
                    ? termFactory.getImmutableExpression(this, newTerm)
                    : termFactory.getDBBooleanConstant(!isNull);
        }
        else {
            ImmutableFunctionalTerm newFunctionalTerm = (ImmutableFunctionalTerm) newTerm;
            // TODO: try to optimize
            return termFactory.getImmutableExpression(this, newFunctionalTerm);
        }
    }

    @Override
    public boolean isNullable(ImmutableSet<Integer> nullableIndexes) {
        return false;
    }
}
