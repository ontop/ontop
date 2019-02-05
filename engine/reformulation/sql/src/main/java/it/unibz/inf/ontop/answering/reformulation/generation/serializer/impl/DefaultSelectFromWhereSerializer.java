package it.unibz.inf.ontop.answering.reformulation.generation.serializer.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SQLRelationVisitor;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SQLSerializedQuery;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SelectFromWhere;
import it.unibz.inf.ontop.answering.reformulation.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.model.term.Variable;

@Singleton
public class DefaultSelectFromWhereSerializer implements SelectFromWhereSerializer {

    @Inject
    private DefaultSelectFromWhereSerializer() {
    }

    @Override
    public QuerySerialization serialize(SelectFromWhere selectFromWhere) {
        return selectFromWhere.acceptVisitor(
                new DefaultSQLRelationVisitingSerializer());
    }

    /**
     * Mutable: one instance per SQL query to generate
     */
    protected static class DefaultSQLRelationVisitingSerializer implements SQLRelationVisitor<QuerySerialization> {

        @Override
        public QuerySerialization visit(SelectFromWhere selectFromWhere) {
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
        public QuerySerialization visit(SQLSerializedQuery sqlSerializedQuery) {
            return new QuerySerializationImpl(sqlSerializedQuery.getSQLString(), sqlSerializedQuery.getVariableNames());
        }
    }


    protected static class QuerySerializationImpl implements QuerySerialization {

        private final String string;
        private final ImmutableMap<Variable, String> variableNames;

        public QuerySerializationImpl(String string, ImmutableMap<Variable, String> variableNames) {
            this.string = string;
            this.variableNames = variableNames;
        }

        @Override
        public String getString() {
            return string;
        }

        @Override
        public ImmutableMap<Variable, String> getVariableNames() {
            return variableNames;
        }
    }

}
