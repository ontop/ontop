package it.unibz.inf.ontop.answering.reformulation.generation.algebra;


public interface SQLRelationVisitor<T> {

    T visit(SelectFromWhere selectFromWhere);

    T visit(SQLSerializedQuery sqlSerializedQuery);
}
