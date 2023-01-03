package it.unibz.inf.ontop.generation.algebra;

import it.unibz.inf.ontop.model.term.ImmutableExpression;

import java.util.Optional;

/**
 * Abstraction for LeftJoinRelation and explicit InnerJoinRelation
 */
public interface BinaryJoinExpression extends SQLExpression {

    SQLExpression getLeft();
    SQLExpression getRight();

    Optional<ImmutableExpression> getFilterCondition();

}
