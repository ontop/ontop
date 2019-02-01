package it.unibz.inf.ontop.answering.reformulation.generation.serializer.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SQLRelationVisitor;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SQLSerializedQuery;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SelectFromWhere;
import it.unibz.inf.ontop.answering.reformulation.generation.serializer.SelectFromWhereSerializer;

@Singleton
public class DefaultSelectFromWhereSerializer implements SelectFromWhereSerializer {

    @Inject
    private DefaultSelectFromWhereSerializer() {
    }

    @Override
    public String serialize(SelectFromWhere selectFromWhere) {
        return selectFromWhere.acceptVisitor(
                new DefaultSQLRelationVisitingSerializer());
    }


    /**
     * Mutable: one instance per SQL query to generate
     */
    protected static class DefaultSQLRelationVisitingSerializer implements SQLRelationVisitor<String> {

        @Override
        public String visit(SelectFromWhere selectFromWhere) {
            throw new RuntimeException("TODO: implement the serialization of a SelectFromWhere");
            //		if (queryModifiers.hasModifiers()) {
//			//List<Variable> groupby = queryProgram.getQueryModifiers().getGroupConditions();
//			// if (!groupby.isEmpty()) {
//			// subquery += "\n" + sqladapter.sqlGroupBy(groupby, "") + " " +
//			// havingStr + "\n";
//			// }
//			// List<OrderCondition> conditions =
//			// query.getQueryModifiers().getSortConditions();
//
//			long limit = queryModifiers.getLimit();
//			long offset = queryModifiers.getOffset();
//			List<OrderCondition> conditions = queryModifiers.getSortConditions();
//
//			final String modifier;
//			if (!conditions.isEmpty()) {
//				modifier = sqladapter.sqlOrderByAndSlice(conditions, OUTER_VIEW_NAME, limit, offset) + "\n";
//			}
//			else if (limit != -1 || offset != -1) {
//				modifier = sqladapter.sqlSlice(limit, offset) + "\n";
//			}
//			else {
//				modifier = "";
//			}
//
//			resultingQuery = "SELECT *\n" +
//					"FROM " + inBrackets("\n" + queryString + "\n") + " " + OUTER_VIEW_NAME + "\n" +
//					modifier;
//		}
//		else {
//			resultingQuery = queryString;
//		}
        }

        @Override
        public String visit(SQLSerializedQuery sqlSerializedQuery) {
            return sqlSerializedQuery.getSQLString();
        }
    }


}
