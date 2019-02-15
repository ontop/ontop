package it.unibz.inf.ontop.answering.reformulation.generation.algebra;

public interface SQLExpression {

    <T> T acceptVisitor(SQLRelationVisitor<T> visitor);
}
