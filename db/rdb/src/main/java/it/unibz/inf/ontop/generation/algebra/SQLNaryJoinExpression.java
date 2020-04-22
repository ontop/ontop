package it.unibz.inf.ontop.generation.algebra;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableExpression;

import java.util.Optional;

public interface SQLNaryJoinExpression extends SQLExpression {

    ImmutableList <SQLExpression> getJoinedExpressions();

    Optional<ImmutableExpression> getFilterCondition();

}
