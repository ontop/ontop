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
import it.unibz.inf.ontop.answering.reformulation.generation.dialect.SQLDialectAdapter;
import it.unibz.inf.ontop.answering.reformulation.generation.serializer.SQLTermSerializer;
import it.unibz.inf.ontop.answering.reformulation.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.iq.node.OrderByNode;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Singleton
public class DefaultSelectFromWhereSerializer implements SelectFromWhereSerializer {

    private final SQLTermSerializer sqlTermSerializer;
    private final SQLDialectAdapter dialectAdapter;

    @Inject
    protected DefaultSelectFromWhereSerializer(SQLTermSerializer sqlTermSerializer, SQLDialectAdapter dialectAdapter) {
        this.sqlTermSerializer = sqlTermSerializer;
        this.dialectAdapter = dialectAdapter;
    }

    @Override
    public QuerySerialization serialize(SelectFromWhereWithModifiers selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(
                new DefaultSQLRelationVisitingSerializer(sqlTermSerializer, dialectAdapter, dbParameters.getQuotedIDFactory()));
    }

    /**
     * Mutable: one instance per SQL query to generate
     */
    protected static class DefaultSQLRelationVisitingSerializer implements SQLRelationVisitor<QuerySerialization> {

        protected static final String VIEW_PREFIX = "v";
        private static final String SELECT_FROM_WHERE_MODIFIERS_TEMPLATE = "SELECT %s%s\nFROM %s\n%s%s%s";
        private final AtomicInteger viewCounter;
        protected final SQLTermSerializer sqlTermSerializer;
        protected final SQLDialectAdapter dialectAdapter;
        protected final QuotedIDFactory idFactory;

        protected DefaultSQLRelationVisitingSerializer(SQLTermSerializer sqlTermSerializer, SQLDialectAdapter dialectAdapter,
                                                       QuotedIDFactory idFactory) {
            this.sqlTermSerializer = sqlTermSerializer;
            this.dialectAdapter = dialectAdapter;
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

            String projectionString = serializeProjection(selectFromWhere.getProjectedVariables(),
                    projectedColumnMap, selectFromWhere.getSubstitution(), fromColumnMap);

            String fromString = serializeFrom(serializedFromEntries);

            String whereString = selectFromWhere.getWhereExpression()
                    .map(e -> sqlTermSerializer.serialize(e, fromColumnMap))
                    .map(s -> String.format("WHERE %s\n", s))
                    .orElse("");

            String orderByString = serializeOrderBy(selectFromWhere.getSortConditions(), fromColumnMap);
            String sliceString = serializeSlice(selectFromWhere.getLimit(), selectFromWhere.getOffset());

            String sql = String.format(SELECT_FROM_WHERE_MODIFIERS_TEMPLATE, distinctString, projectionString,
                    fromString, whereString, orderByString, sliceString);

            return new QuerySerializationImpl(sql, projectedColumnMap);
        }

        protected RelationID generateFreshViewAlias() {
            return idFactory.createRelationID(null, VIEW_PREFIX + viewCounter.incrementAndGet());
        }

        private QualifiedAttributeID createQualifiedAttributeId(RelationID relationID, String columnName) {
            return new QualifiedAttributeID(relationID, idFactory.createAttributeID(columnName));
        }

        /**
         * TODO: simplify it
         */
        private ImmutableMap<Variable, String> extractProjectionColumnMap(
                ImmutableSortedSet<Variable> projectedVariables, ImmutableMap<Variable, QualifiedAttributeID> fromColumnMap) {
            // Mutable, initialized with the column names projected by the "from" relations
            Set<String> quotedColumnNames = fromColumnMap.values().stream()
                    .map(QualifiedAttributeID::getSQLRendering)
                    .collect(Collectors.toSet());

            return projectedVariables.stream()
                    .collect(ImmutableCollectors.toMap(
                            v -> v,
                            v -> {
                                if (fromColumnMap.containsKey(v))
                                    return sqlTermSerializer.serialize(v, fromColumnMap);

                                String newColumnName = dialectAdapter.nameTopVariable(v.getName(), quotedColumnNames);
                                quotedColumnNames.add(newColumnName);
                                return newColumnName;
                            }));
        }


        protected String serializeProjection(ImmutableSortedSet<Variable> projectedVariables,
                                             ImmutableMap<Variable, String> projectedColumnMap,
                                             ImmutableSubstitution<? extends ImmutableTerm> substitution,
                                             ImmutableMap<Variable, QualifiedAttributeID> fromColumnMap) {
            // Mainly for ASK queries
            if (projectedVariables.isEmpty())
                return "1 AS uselessVariable";

            return projectedVariables.stream()
                    .map(v -> Optional.ofNullable(substitution.get(v))
                            .map(d -> sqlTermSerializer.serialize(d, fromColumnMap))
                            .map(s -> s + " AS " + projectedColumnMap.get(v))
                            .orElseGet(() -> projectedColumnMap.get(v)))
                    .collect(Collectors.joining(", "));
        }

        protected String serializeFrom(ImmutableList<Map.Entry<RelationID, QuerySerialization>> serializedFromEntries) {
            return serializedFromEntries.stream()
                    .map(e -> String.format("(%s) %s", e.getValue().getString(), e.getKey().getSQLRendering()))
                    .collect(Collectors.joining(",\n"));
        }

        protected String serializeOrderBy(ImmutableList<OrderByNode.OrderComparator> sortConditions,
                                        ImmutableMap<Variable, QualifiedAttributeID> fromColumnMap) {
            if (sortConditions.isEmpty())
                return "";

            String conditionString = sortConditions.stream()
                    .map(c -> sqlTermSerializer.serialize(c.getTerm(), fromColumnMap)
                            + (c.isAscending() ? " NULLS FIRST" : " DESC NULLS LAST"))
                    .collect(Collectors.joining(", "));

            return String.format("ORDER BY %s\n", conditionString);
        }

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private String serializeSlice(Optional<Long> limit, Optional<Long> offset) {
            return dialectAdapter.sqlSlice(limit.orElse(-1L), offset.orElse(-1L));
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
