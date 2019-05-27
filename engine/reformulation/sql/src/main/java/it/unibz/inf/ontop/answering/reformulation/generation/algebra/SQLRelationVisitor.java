package it.unibz.inf.ontop.answering.reformulation.generation.algebra;


public interface SQLRelationVisitor<T> {

    T visit(SelectFromWhereWithModifiers selectFromWhere);

    T visit(SQLSerializedQuery sqlSerializedQuery);

    T visit(SQLTable sqlTable);
}
