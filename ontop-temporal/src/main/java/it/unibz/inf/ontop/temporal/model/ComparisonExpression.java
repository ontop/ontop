package it.unibz.inf.ontop.temporal.model;

import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;

public interface ComparisonExpression extends AtomicExpression {

    VariableOrGroundTerm getLeftOperand();
    VariableOrGroundTerm getRightOperand();
}
