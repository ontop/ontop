package it.unibz.inf.ontop.generation.algebra;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.Variable;

public interface SQLUnionExpression extends SQLExpression {

    ImmutableSet<Variable> getProjectedVariables();

    ImmutableList<? extends SQLExpression> getSubExpressions();
}
