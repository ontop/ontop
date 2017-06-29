package it.unibz.inf.ontop.evaluator;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.ImmutableExpression;
import it.unibz.inf.ontop.model.ImmutableTerm;
import it.unibz.inf.ontop.model.Variable;

public interface TermNullabilityEvaluator {

    boolean isNullable(ImmutableTerm term, ImmutableSet<Variable> nullableVariables);

    boolean isFilteringNullValue(ImmutableExpression expression, Variable variable);
}
