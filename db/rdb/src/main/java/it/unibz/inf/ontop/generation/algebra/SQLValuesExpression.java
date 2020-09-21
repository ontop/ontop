package it.unibz.inf.ontop.generation.algebra;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.Variable;

public interface SQLValuesExpression extends SQLExpression {


    ImmutableList<Variable> getOrderedVariables();

    ImmutableList<ImmutableList<Constant>> getValues();
}
