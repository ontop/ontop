package it.unibz.inf.ontop.answering.reformulation.generation.algebra;

import it.unibz.inf.ontop.model.term.ImmutableExpression;

import java.util.Optional;

/**
 * Abstraction for LeftJoinRelation and explicit InnerJoinRelation
 */
public interface BinaryJoinRelation extends SQLRelation {

    SQLRelation getLeft();
    SQLRelation getRight();

    Optional<ImmutableExpression> getFilterCondition();

}
