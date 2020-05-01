package it.unibz.inf.ontop.generation.algebra;

/**
 * See SQLAlgebraFactory for creating new instances.
 */
public interface SQLExpression {

    <T> T acceptVisitor(SQLRelationVisitor<T> visitor);
}
