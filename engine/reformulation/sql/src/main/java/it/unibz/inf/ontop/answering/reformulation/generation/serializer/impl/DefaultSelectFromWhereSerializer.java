package it.unibz.inf.ontop.answering.reformulation.generation.serializer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.*;
import it.unibz.inf.ontop.answering.reformulation.generation.dialect.SQLDialectAdapter;
import it.unibz.inf.ontop.answering.reformulation.generation.serializer.SQLTermSerializer;
import it.unibz.inf.ontop.answering.reformulation.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.RawQuotedIDFactory;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
public class DefaultSelectFromWhereSerializer implements SelectFromWhereSerializer {

    protected final SQLTermSerializer sqlTermSerializer;
    protected final SQLDialectAdapter dialectAdapter;

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
        private static final String SELECT_FROM_WHERE_MODIFIERS_TEMPLATE = "SELECT %s%s\nFROM %s\n%s%s%s%s";
        private final AtomicInteger viewCounter;
        protected final SQLTermSerializer sqlTermSerializer;
        protected final SQLDialectAdapter dialectAdapter;
        protected final QuotedIDFactory idFactory;
        private final QuotedIDFactory rawIdFactory;

        protected DefaultSQLRelationVisitingSerializer(SQLTermSerializer sqlTermSerializer, SQLDialectAdapter dialectAdapter,
                                                       QuotedIDFactory idFactory) {
            this.sqlTermSerializer = sqlTermSerializer;
            this.dialectAdapter = dialectAdapter;
            this.idFactory = idFactory;
            this.rawIdFactory = new RawQuotedIDFactory(idFactory);
            this.viewCounter = new AtomicInteger(0);
        }

        @Override
        public QuerySerialization visit(SelectFromWhereWithModifiers selectFromWhere) {

            QuerySerialization fromQuerySerialization = getSQLSerializationForChild(selectFromWhere.getFromSQLExpression());

            // Assumes that from expressions all use different variables
            ImmutableMap<Variable, QualifiedAttributeID> fromColumnIDs = fromQuerySerialization.getColumnIDs();

            ImmutableMap<Variable, QualifiedAttributeID> columnsInProjectionIds = extractProjectionColumnMap(
                    selectFromWhere.getProjectedVariables(), fromColumnIDs);

            String distinctString = selectFromWhere.isDistinct() ? "DISTINCT " : "";

            String projectionString = serializeProjection(selectFromWhere.getProjectedVariables(),
                    columnsInProjectionIds, selectFromWhere.getSubstitution(), fromColumnIDs);

            String fromString = fromQuerySerialization.getString();

            String whereString = selectFromWhere.getWhereExpression()
                    .map(e -> sqlTermSerializer.serialize(e, fromColumnIDs))
                    .map(s -> String.format("WHERE %s\n", s))
                    .orElse("");

            String groupByString = serializeGroupBy(selectFromWhere.getGroupByVariables(), fromColumnIDs);

            String orderByString = serializeOrderBy(selectFromWhere.getSortConditions(), fromColumnIDs);
            String sliceString = serializeSlice(selectFromWhere.getLimit(), selectFromWhere.getOffset());

            String sql = String.format(SELECT_FROM_WHERE_MODIFIERS_TEMPLATE, distinctString, projectionString,
                    fromString, whereString, groupByString, orderByString, sliceString);

            // Creates an alias for this SQLExpression and uses it for the projected columns
            RelationID alias = generateFreshViewAlias();

            return new QuerySerializationImpl(sql, createAliasMap(alias, columnsInProjectionIds.keySet()));
        }

        protected RelationID generateFreshViewAlias() {
            return idFactory.createRelationID(null, VIEW_PREFIX + viewCounter.incrementAndGet());
        }

        private ImmutableMap<Variable, QualifiedAttributeID> createAliasMap(RelationID alias, ImmutableSet<Variable> stream) {
            return stream.stream()
                .collect(ImmutableCollectors.toMap(
                    Function.identity(),
                    v -> new QualifiedAttributeID(alias, rawIdFactory.createAttributeID(v.getName()))));
        }


        private ImmutableMap<Variable, QualifiedAttributeID> extractProjectionColumnMap(
                ImmutableSortedSet<Variable> projectedVariables, ImmutableMap<Variable, QualifiedAttributeID> fromColumnMap) {
            // Mutable, initialized with the column names projected by the "from" relations
            Set<String> quotedColumnNames = fromColumnMap.values().stream()
                    .map(QualifiedAttributeID::getSQLRendering)
                    .collect(Collectors.toSet());

            return projectedVariables.stream()
                    .collect(ImmutableCollectors.toMap(
                            Function.identity(),
                            v -> {
                                if (fromColumnMap.containsKey(v))
                                    return fromColumnMap.get(v);

                                String newColumnName = dialectAdapter.nameTopVariable(v.getName(), quotedColumnNames);
                                quotedColumnNames.add(newColumnName);
                                return new QualifiedAttributeID(null, rawIdFactory.createAttributeID(newColumnName));
                            }));
        }


        protected String serializeProjection(ImmutableSortedSet<Variable> projectedVariables,
                                             ImmutableMap<Variable, QualifiedAttributeID> projectedColumnMap,
                                             ImmutableSubstitution<? extends ImmutableTerm> substitution,
                                             ImmutableMap<Variable, QualifiedAttributeID> fromColumnMap) {
            // Mainly for ASK queries
            if (projectedVariables.isEmpty())
                return "1 AS uselessVariable";

            return projectedVariables.stream()
                    .map(v -> Optional.ofNullable(substitution.get(v))
                            .map(d -> sqlTermSerializer.serialize(d, fromColumnMap))
                            .map(s -> s + " AS " + projectedColumnMap.get(v).getSQLRendering())
                            .orElseGet(() -> projectedColumnMap.get(v).getSQLRendering() +
                                    " AS " + rawIdFactory.createAttributeID(v.getName()).getSQLRendering()))
                    .collect(Collectors.joining(", "));
        }

        protected String serializeGroupBy(ImmutableSet<Variable> groupByVariables,
                                          ImmutableMap<Variable, QualifiedAttributeID> fromColumnMap) {
            if (groupByVariables.isEmpty())
                return "";

            String variableString = groupByVariables.stream()
                    .map(v -> sqlTermSerializer.serialize(v, fromColumnMap))
                    .collect(Collectors.joining(", "));

            return String.format("GROUP BY %s\n", variableString);
        }

        protected String serializeOrderBy(ImmutableList<SQLOrderComparator> sortConditions,
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

            RelationID alias = generateFreshViewAlias();
            ImmutableMap<Variable, QualifiedAttributeID> columnIDs = sqlSerializedQuery.getColumnNames().entrySet().stream()
                    .collect(ImmutableCollectors.toMap(
                            Map.Entry::getKey,
                            e -> new QualifiedAttributeID(alias, e.getValue())
                    ));

            String sqlSubString = String.format("(%s) %s",sqlSerializedQuery.getSQLString(), alias.getSQLRendering());

            return new QuerySerializationImpl(sqlSubString, columnIDs);
        }

        @Override
        public QuerySerialization visit(SQLTable sqlTable) {

            RelationID aliasId = generateFreshViewAlias();
            RelationDefinition relationDefinition = sqlTable.getRelationDefinition();
            String relationRendering = relationDefinition.getAtomPredicate().getName();
            String sqlSubString = String.format("%s %s", relationRendering, aliasId.getSQLRendering());

            return new QuerySerializationImpl(sqlSubString,
                    sqlTable.getArgumentMap().entrySet().stream()
                            .collect(ImmutableCollectors.toMap(
                                    // Ground terms must have been already removed from atoms
                                    e -> (Variable) e.getValue(),
                                    e -> new QualifiedAttributeID(aliasId, relationDefinition.getAttribute(e.getKey() + 1).getID())
                            )));
        }

        @Override
        public QuerySerialization visit(SQLNaryJoinExpression sqlNaryJoinExpression) {
            ImmutableList<QuerySerialization> querySerializationList = sqlNaryJoinExpression.getJoinedExpressions().stream()
                    .map(this::getSQLSerializationForChild)
                    .collect(ImmutableCollectors.toList());

            String sqlSubString = querySerializationList.stream()
                    .map(QuerySerialization::getString)
                    .collect(Collectors.joining(", "));

            ImmutableMap<Variable, QualifiedAttributeID> columnIDs = querySerializationList.stream()
                            .flatMap(m -> m.getColumnIDs().entrySet().stream())
                            .collect(ImmutableCollectors.toMap());

            return new QuerySerializationImpl(sqlSubString, columnIDs);
        }

        @Override
        public QuerySerialization visit(SQLUnionExpression sqlUnionExpression) {
            ImmutableList<QuerySerialization> querySerializationList = sqlUnionExpression.getSubExpressions().stream()
                    .map(e -> e.acceptVisitor(this))
                    .collect(ImmutableCollectors.toList());

            String sqlSubString = querySerializationList.stream()
                    .map(QuerySerialization::getString)
                    .collect(Collectors.joining("UNION ALL \n"));

            RelationID alias = generateFreshViewAlias();
            sqlSubString = String.format("(%s) %s",sqlSubString, alias.getSQLRendering());

            return new QuerySerializationImpl(sqlSubString, createAliasMap(alias, sqlUnionExpression.getProjectedVariables()));
        }

        //this function is required in case at least one of the children is
        // SelectFromWhereWithModifiers expression
        private QuerySerialization getSQLSerializationForChild(SQLExpression expression){
            if (expression instanceof SelectFromWhereWithModifiers){
                QuerySerialization serialization = expression.acceptVisitor(this);

                RelationID alias = generateFreshViewAlias();
                String sql = String.format("(%s) %s",serialization.getString(), alias.getSQLRendering());

                return new QuerySerializationImpl(sql, createAliasMap(alias, serialization.getColumnIDs().keySet()));
            }
            return expression.acceptVisitor(this);
        }

        @Override
        public QuerySerialization visit(SQLInnerJoinExpression sqlInnerJoinExpression) {
            return visit(sqlInnerJoinExpression, "JOIN");
        }

        @Override
        public QuerySerialization visit(SQLLeftJoinExpression sqlLeftJoinExpression) {
            return visit(sqlLeftJoinExpression, "LEFT OUTER JOIN");
        }

        /**
         * NB: the systematic use of ON conditions for inner and left joins saves us from putting parentheses.
         *
         * Indeed since a join expression with a ON is always "CHILD_1 SOME_JOIN CHILD_2 ON COND",
         * the decomposition is unambiguous just following this pattern.
         *
         * For instance, "T1 LEFT JOIN T2 INNER JOIN T3 ON 1=1 ON 2=2"
         * is clearly equivalent to "T1 LEFT JOIN (T2 INNER JOIN T3)"
         * as the latest ON condition ("ON 2=2") can only be attached to the left join, which means that "T2 INNER JOIN T3 ON 1=1"
         * is the right child of the left join.
         *
         */
        protected QuerySerialization visit(BinaryJoinExpression binaryJoinExpression, String operatorString) {
            QuerySerialization left = getSQLSerializationForChild(binaryJoinExpression.getLeft());
            QuerySerialization right = getSQLSerializationForChild(binaryJoinExpression.getRight());

            String sqlSubString = String.format("%s\n %s \n%s ",left.getString(), operatorString, right.getString());

            ImmutableMap<Variable, QualifiedAttributeID> columnIDs = ImmutableList.of(left,right).stream()
                            .flatMap(m -> m.getColumnIDs().entrySet().stream())
                            .collect(ImmutableCollectors.toMap());

            String onString = binaryJoinExpression.getFilterCondition()
                    .map(e -> sqlTermSerializer.serialize(e, columnIDs))
                    .map(s -> String.format("ON %s ", s))
                    .orElse("ON 1 = 1 ");

            return new QuerySerializationImpl(sqlSubString + onString, columnIDs);
        }

        @Override
        public QuerySerialization visit(SQLOneTupleDummyQueryExpression sqlOneTupleDummyQueryExpression) {
            String fromString = dialectAdapter.getTrueTable()
                    .map(t -> String.format("FROM %s", t))
                    .orElse("");

            String sqlSubString = String.format("(SELECT 1 %s) tdummy", fromString);
            return new QuerySerializationImpl(sqlSubString, ImmutableMap.of());
        }

    }


    protected static class QuerySerializationImpl implements QuerySerialization {

        private final String string;
        private final ImmutableMap<Variable, QualifiedAttributeID> columnIDs;

        public QuerySerializationImpl(String string, ImmutableMap<Variable, QualifiedAttributeID> columnIDs) {
            this.string = string;
            this.columnIDs = columnIDs;
        }

        @Override
        public String getString() {
            return string;
        }

        @Override
        public ImmutableMap<Variable, QualifiedAttributeID> getColumnIDs() {
            return columnIDs;
        }
    }

}
