package it.unibz.inf.ontop.model.term.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.model.term.IncrementalEvaluation;
import it.unibz.inf.ontop.model.term.Variable;

public abstract class AbstractNonNullConstant extends AbstractNonFunctionalTerm {

    @Override
    public IncrementalEvaluation evaluateIsNotNull(VariableNullability variableNullability) {
        return IncrementalEvaluation.declareIsTrue();
    }

    @Override
    public boolean isNullable(ImmutableSet<Variable> nullableVariables) {
        return false;
    }
}
