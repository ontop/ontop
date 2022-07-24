package it.unibz.inf.ontop.generation.algebra;


public interface SQLRelationVisitor<T> {

    T visit(SelectFromWhereWithModifiers selectFromWhere);

    T visit(SQLSerializedQuery sqlSerializedQuery);

    T visit(SQLTable sqlTable);

    T visit(SQLNaryJoinExpression sqlNaryJoinExpression);

    T visit(SQLUnionExpression sqlUnionExpression);

    T visit(SQLInnerJoinExpression sqlInnerJoinExpression);

    T visit(SQLLeftJoinExpression sqlLeftJoinExpression);

    T visit(SQLOneTupleDummyQueryExpression sqlOneTupleDummyQueryExpression);

    T visit(SQLValuesExpression sqlValuesExpression);
}
