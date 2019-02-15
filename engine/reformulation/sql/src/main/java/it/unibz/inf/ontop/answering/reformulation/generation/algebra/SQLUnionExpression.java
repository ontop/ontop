package it.unibz.inf.ontop.answering.reformulation.generation.algebra;

import com.google.common.collect.ImmutableList;

public interface SQLUnionExpression extends SQLExpression {

    ImmutableList<? extends SQLExpression> getSubExpressions();
}
