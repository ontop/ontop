package it.unibz.inf.ontop.answering.reformulation.generation.serializer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SQLRelationVisitor;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SQLSerializedQuery;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.answering.reformulation.generation.serializer.SQLTermSerializer;
import it.unibz.inf.ontop.answering.reformulation.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.iq.node.OrderByNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
public class DefaultSelectFromWhereSerializer implements SelectFromWhereSerializer {

    private final SQLTermSerializer sqlTermSerializer;

    @Inject
    private DefaultSelectFromWhereSerializer(SQLTermSerializer sqlTermSerializer) {
        this.sqlTermSerializer = sqlTermSerializer;
    }

    @Override
    public QuerySerialization serialize(SelectFromWhereWithModifiers selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(
                new DefaultSQLRelationVisitingSerializer(sqlTermSerializer, dbParameters.getQuotedIDFactory()));
    }

    /**
     * Mutable: one instance per SQL query to generate
     */
    protected static class DefaultSQLRelationVisitingSerializer implements SQLRelationVisitor<QuerySerialization> {

        protected static final String VIEW_PREFIX = "v";
        private static final String SELECT_FROM_WHERE_MODIFIERS_TEMPLATE = "SELECT %s%s\nFROM %s\n%s%s%s";
        private final AtomicInteger viewCounter;
        private final SQLTermSerializer sqlTermSerializer;
        private final QuotedIDFactory idFactory;

        protected DefaultSQLRelationVisitingSerializer(SQLTermSerializer sqlTermSerializer, QuotedIDFactory idFactory) {
            this.sqlTermSerializer = sqlTermSerializer;
            this.idFactory = idFactory;
            this.viewCounter = new AtomicInteger(0);
        }

        @Override
        public QuerySerialization visit(SelectFromWhereWithModifiers selectFromWhere) {

            ImmutableList<Map.Entry<RelationID, QuerySerialization>> serializedFromEntries =
                    selectFromWhere.getFromSQLExpressions().stream()
                            .map(e -> e.acceptVisitor(this))
                            .map(s -> Maps.immutableEntry(generateFreshViewAlias(), s))
                            .collect(ImmutableCollectors.toList());

            ImmutableMap<RelationID, QuerySerialization> fromMap = serializedFromEntries.stream()
                    .collect(ImmutableCollectors.toMap());

            // Assumes that from expressions all use different variables
            ImmutableMap<Variable, QualifiedAttributeID> fromColumnMap = fromMap.entrySet().stream()
                    .flatMap(fromE -> fromE.getValue().getColumnNames().entrySet().stream()
                            .map(e -> Maps.immutableEntry(e.getKey(), createQualifiedAttributeId(fromE.getKey(), e.getValue()))))
                    .collect(ImmutableCollectors.toMap());

            ImmutableMap<Variable, String> projectedColumnMap = extractProjectionColumnMap(
                    selectFromWhere.getProjectedVariables(), fromColumnMap);

            String distinctString = selectFromWhere.isDistinct() ? "DISTINCT " : "";

            String projectionString = serializeProjection(projectedColumnMap, selectFromWhere.getSubstitution());

            String fromString = serializeFrom(fromMap);

            String whereString = selectFromWhere.getWhereExpression()
                    .map(e -> serializeBooleanExpression(e, fromColumnMap))
                    .map(s -> String.format("WHERE %s\n", s))
                    .orElse("");

            String orderByString = serializeOrderBy(selectFromWhere.getSortConditions(), fromColumnMap);
            String sliceString = serializeSlice(selectFromWhere.getLimit(), selectFromWhere.getOffset());

            String sql = String.format(SELECT_FROM_WHERE_MODIFIERS_TEMPLATE, distinctString, projectionString,
                    fromString, whereString, orderByString, sliceString);

            return new QuerySerializationImpl(sql, projectedColumnMap);

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

        protected RelationID generateFreshViewAlias() {
            throw new RuntimeException("TODO: implement generateFreshViewAlias");
        }

        private QualifiedAttributeID createQualifiedAttributeId(RelationID relationID, String columnName) {
            throw new RuntimeException("TODO: implement createQualifiedAttributeId");
        }


        protected String serializeProjection(ImmutableMap<Variable, String> projectedColumnMap,
                                             ImmutableSubstitution<? extends ImmutableTerm> substitution) {
            throw new RuntimeException("TODO: implement serializeProjection");

        }

        protected String serializeFrom(ImmutableMap<RelationID, QuerySerialization> fromMap) {
            throw new RuntimeException("TODO: implement serializeFrom");
        }

        /**
         * TODO: implement seriously
         */
        private String serializeOrderBy(ImmutableList<OrderByNode.OrderComparator> sortConditions,
                                        ImmutableMap<Variable, QualifiedAttributeID> fromColumnMap) {
            return "";
        }

        /**
         * TODO: implement seriously
         */
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private String serializeSlice(Optional<Long> limit, Optional<Long> offset) {
            return "";
        }

        private String serializeBooleanExpression(ImmutableExpression expression,
                                                  ImmutableMap<Variable, QualifiedAttributeID> columnMap) {
            throw new RuntimeException("TODO: implement serializeBooleanExpression");
        }

        private ImmutableMap<Variable, String> extractProjectionColumnMap(
                ImmutableSortedSet<Variable> projectedVariables, ImmutableMap<Variable, QualifiedAttributeID> fromColumnMap) {
            throw new RuntimeException("TODO: implement extractProjectionColumnMap");
        }

        @Override
        public QuerySerialization visit(SQLSerializedQuery sqlSerializedQuery) {
            return new QuerySerializationImpl(sqlSerializedQuery.getSQLString(), sqlSerializedQuery.getColumnNames());
        }
    }


    protected static class QuerySerializationImpl implements QuerySerialization {

        private final String string;
        private final ImmutableMap<Variable, String> columnNames;

        public QuerySerializationImpl(String string, ImmutableMap<Variable, String> columnNames) {
            this.string = string;
            this.columnNames = columnNames;
        }

        @Override
        public String getString() {
            return string;
        }

        @Override
        public ImmutableMap<Variable, String> getColumnNames() {
            return columnNames;
        }
    }

}
