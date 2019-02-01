package it.unibz.inf.ontop.answering.reformulation.generation.algebra;

public interface SQLRelation {

    <T> T acceptVisitor(SQLRelationVisitor<T> visitor);
}
