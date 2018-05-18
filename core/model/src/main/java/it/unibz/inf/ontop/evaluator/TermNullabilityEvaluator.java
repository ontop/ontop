package it.unibz.inf.ontop.evaluator;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;

public interface TermNullabilityEvaluator {

    boolean isNullable(ImmutableTerm term, ImmutableSet<Variable> nullableVariables);

    boolean isFilteringNullValue(ImmutableExpression expression, Variable variable);

    /**
     * tightVariables: if one is null the others as well
     */
    boolean isFilteringNullValues(ImmutableExpression expression, ImmutableSet<Variable> tightVariables);
}
