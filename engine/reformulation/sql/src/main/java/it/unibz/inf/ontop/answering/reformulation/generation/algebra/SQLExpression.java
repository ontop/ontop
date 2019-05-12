package it.unibz.inf.ontop.answering.reformulation.generation.algebra;

/**
 * See SQLAlgebraFactory for creating new instances.
 */
public interface SQLExpression {

    <T> T acceptVisitor(SQLRelationVisitor<T> visitor);
}
