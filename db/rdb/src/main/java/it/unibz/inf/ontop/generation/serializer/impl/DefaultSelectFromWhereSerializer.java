package it.unibz.inf.ontop.generation.serializer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.generation.algebra.*;
import it.unibz.inf.ontop.generation.serializer.SQLSerializationException;
import it.unibz.inf.ontop.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
public class DefaultSelectFromWhereSerializer implements SelectFromWhereSerializer {

    protected final SQLTermSerializer sqlTermSerializer;

    @Inject
    private DefaultSelectFromWhereSerializer(TermFactory termFactory) {
        this(new DefaultSQLTermSerializer(termFactory));
    }

    protected DefaultSelectFromWhereSerializer(SQLTermSerializer sqlTermSerializer) {
        this.sqlTermSerializer = sqlTermSerializer;
    }

    @Override
    public QuerySerialization serialize(SelectFromWhereWithModifiers selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(
                new DefaultRelationVisitingSerializer(dbParameters.getQuotedIDFactory()));
    }

    @Override
    public SQLTermSerializer getTermSerializer() {
        return sqlTermSerializer;
    }

    /**
     * Mutable: one instance per SQL query to generate
     */
    protected class DefaultRelationVisitingSerializer implements SQLRelationVisitor<QuerySerialization> {

        private static final String VIEW_PREFIX = "v";
        private static final String SELECT_FROM_WHERE_MODIFIERS_TEMPLATE = "SELECT %s%s\nFROM %s\n%s%s%s%s";

        protected final QuotedIDFactory idFactory;

        private final AtomicInteger viewCounter;

        protected DefaultRelationVisitingSerializer(QuotedIDFactory idFactory) {
            this.idFactory = idFactory;
            this.viewCounter = new AtomicInteger(0);
        }

        @Override
        public QuerySerialization visit(SelectFromWhereWithModifiers selectFromWhere) {

            QuerySerialization fromQuerySerialization = getSQLSerializationForChild(selectFromWhere.getFromSQLExpression());

            ImmutableMap<Variable, QuotedID> variableAliases = createVariableAliases(selectFromWhere.getProjectedVariables());

            String distinctString = selectFromWhere.isDistinct() ? "DISTINCT " : "";

            ImmutableMap<Variable, QualifiedAttributeID> columnIDs = fromQuerySerialization.getColumnIDs();
            String projectionString = serializeProjection(selectFromWhere.getProjectedVariables(),
                                variableAliases, selectFromWhere.getSubstitution(), columnIDs);

            String fromString = fromQuerySerialization.getString();

            // TODO: if selectFromWhere.getLimit is 0, then replace it with an additional filter 0 = 1
            String whereString = selectFromWhere.getWhereExpression()
                    .map(e -> sqlTermSerializer.serialize(e, columnIDs))
                    .map(s -> String.format("WHERE %s\n", s))
                    .orElse("");

            String groupByString = serializeGroupBy(selectFromWhere.getGroupByVariables(), columnIDs);
            String orderByString = serializeOrderBy(selectFromWhere.getSortConditions(), columnIDs);
            String sliceString = serializeSlice(selectFromWhere.getLimit(), selectFromWhere.getOffset(),
                    selectFromWhere.getSortConditions().isEmpty());

            String sql = String.format(SELECT_FROM_WHERE_MODIFIERS_TEMPLATE, distinctString, projectionString,
                    fromString, whereString, groupByString, orderByString, sliceString);

            // Creates an alias for this SQLExpression and uses it for the projected columns
            RelationID alias = generateFreshViewAlias();
            return new QuerySerializationImpl(sql, attachRelationAlias(alias, variableAliases));
        }

        protected RelationID generateFreshViewAlias() {
            return idFactory.createRelationID(VIEW_PREFIX + viewCounter.incrementAndGet());
        }

        ImmutableMap<Variable, QualifiedAttributeID> attachRelationAlias(RelationID alias, ImmutableMap<Variable, QuotedID> variableAliases) {
            return variableAliases.entrySet().stream()
                    .collect(ImmutableCollectors.toMap(
                            Map.Entry::getKey,
                            e -> new QualifiedAttributeID(alias, e.getValue())));
        }

        private ImmutableMap<Variable, QualifiedAttributeID> replaceRelationAlias(RelationID alias, ImmutableMap<Variable, QualifiedAttributeID> columnIDs) {
            return columnIDs.entrySet().stream()
                    .collect(ImmutableCollectors.toMap(
                            Map.Entry::getKey,
                            e -> new QualifiedAttributeID(alias, e.getValue().getAttribute())));
        }

        private ImmutableMap<Variable, QuotedID> createVariableAliases(ImmutableSet<Variable> variables) {
            AttributeAliasFactory aliasFactory = createAttributeAliasFactory();
            return variables.stream()
                    .collect(ImmutableCollectors.toMap(
                            Function.identity(),
                            v -> aliasFactory.createAttributeAlias(v.getName())));
        }

        protected AttributeAliasFactory createAttributeAliasFactory() {
            return new DefaultAttributeAliasFactory(idFactory);
        }

        protected String serializeDummyTable() {
            return "";
        }

        protected String serializeProjection(ImmutableSortedSet<Variable> projectedVariables, // only for ORDER
                                             ImmutableMap<Variable, QuotedID> variableAliases,
                                             ImmutableSubstitution<? extends ImmutableTerm> substitution,
                                             ImmutableMap<Variable, QualifiedAttributeID> columnIDs) {

            if (projectedVariables.isEmpty())
                return "1 AS uselessVariable";

            return projectedVariables.stream()
                    .map(v -> sqlTermSerializer.serialize(
                            Optional.ofNullable((ImmutableTerm)substitution.get(v)).orElse(v),
                            columnIDs)
                            + " AS " + variableAliases.get(v).getSQLRendering())
                    .collect(Collectors.joining(", "));
        }

        protected String serializeGroupBy(ImmutableSet<Variable> groupByVariables,
                                          ImmutableMap<Variable, QualifiedAttributeID> columnIDs) {
            if (groupByVariables.isEmpty())
                return "";

            String variableString = groupByVariables.stream()
                    .map(v -> sqlTermSerializer.serialize(v, columnIDs))
                    .collect(Collectors.joining(", "));

            return String.format("GROUP BY %s\n", variableString);
        }

        protected String serializeOrderBy(ImmutableList<SQLOrderComparator> sortConditions,
                                        ImmutableMap<Variable, QualifiedAttributeID> columnIDs) {
            if (sortConditions.isEmpty())
                return "";

            String conditionString = sortConditions.stream()
                    .map(c -> sqlTermSerializer.serialize(c.getTerm(), columnIDs)
                            + (c.isAscending() ? " NULLS FIRST" : " DESC NULLS LAST"))
                    .collect(Collectors.joining(", "));

            return String.format("ORDER BY %s\n", conditionString);
        }

        /**
         * There is no standard for these three methods (may not work with many DB engines).
         */
        protected String serializeLimitOffset(long limit, long offset, boolean noSortCondition) {
            return String.format("LIMIT %d, %d", offset, limit);
        }

        //sortConditions added to handle cases of LIMIT without ORDER BY, which need a dummy ORDER BY
        protected String serializeLimit(long limit, boolean noSortCondition) {
            return String.format("LIMIT %d", limit);
        }

        protected String serializeOffset(long offset, boolean noSortCondition) {
            return String.format("OFFSET %d", offset);
        }


        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private String serializeSlice(Optional<Long> limit, Optional<Long> offset, boolean noSortCondition) {
            if (!limit.isPresent() && !offset.isPresent())
                return "";

            if (limit.isPresent() && offset.isPresent())
                return serializeLimitOffset(limit.get(), offset.get(), noSortCondition);

            if (limit.isPresent())
                return serializeLimit(limit.get(), noSortCondition);

            return serializeOffset(offset.get(), noSortCondition);
        }


        @Override
        public QuerySerialization visit(SQLSerializedQuery sqlSerializedQuery) {
            RelationID alias = generateFreshViewAlias();
            String sql = String.format("(%s) %s",sqlSerializedQuery.getSQLString(), alias.getSQLRendering());
            return new QuerySerializationImpl(sql, attachRelationAlias(alias, sqlSerializedQuery.getColumnNames()));
        }

        @Override
        public QuerySerialization visit(SQLTable sqlTable) {
            RelationID alias = generateFreshViewAlias();
            RelationDefinition relation = sqlTable.getRelationDefinition();
            String relationRendering = relation.getAtomPredicate().getName();
            String sql = String.format("%s %s", relationRendering, alias.getSQLRendering());
            return new QuerySerializationImpl(sql, attachRelationAlias(alias, sqlTable.getArgumentMap().entrySet().stream()
                            .collect(ImmutableCollectors.toMap(
                                    // Ground terms must have been already removed from atoms
                                    e -> (Variable) e.getValue(),
                                    e -> relation.getAttribute(e.getKey() + 1).getID()))));
        }

        @Override
        public QuerySerialization visit(SQLNaryJoinExpression sqlNaryJoinExpression) {
            ImmutableList<QuerySerialization> querySerializationList = sqlNaryJoinExpression.getJoinedExpressions().stream()
                    .map(this::getSQLSerializationForChild)
                    .collect(ImmutableCollectors.toList());

            String sql = querySerializationList.stream()
                    .map(QuerySerialization::getString)
                    .collect(Collectors.joining(", "));

            ImmutableMap<Variable, QualifiedAttributeID> columnIDs = querySerializationList.stream()
                            .flatMap(m -> m.getColumnIDs().entrySet().stream())
                            .collect(ImmutableCollectors.toMap());

            return new QuerySerializationImpl(sql, columnIDs);
        }

        @Override
        public QuerySerialization visit(SQLUnionExpression sqlUnionExpression) {
            ImmutableList<QuerySerialization> querySerializationList = sqlUnionExpression.getSubExpressions().stream()
                    .map(e -> e.acceptVisitor(this))
                    .collect(ImmutableCollectors.toList());

            RelationID alias = generateFreshViewAlias();
            String sql = String.format("(%s) %s", querySerializationList.stream()
                    .map(QuerySerialization::getString)
                    .collect(Collectors.joining("UNION ALL \n")), alias.getSQLRendering());

            return new QuerySerializationImpl(sql,
                    replaceRelationAlias(alias, querySerializationList.get(0).getColumnIDs()));
        }

        //this function is required in case at least one of the children is
        // SelectFromWhereWithModifiers expression
        private QuerySerialization getSQLSerializationForChild(SQLExpression expression) {
            if (expression instanceof SelectFromWhereWithModifiers) {
                QuerySerialization serialization = expression.acceptVisitor(this);
                RelationID alias = generateFreshViewAlias();
                String sql = String.format("(%s) %s", serialization.getString(), alias.getSQLRendering());
                return new QuerySerializationImpl(sql,
                        replaceRelationAlias(alias, serialization.getColumnIDs()));
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

            ImmutableMap<Variable, QualifiedAttributeID> columnIDs = ImmutableList.of(left,right).stream()
                            .flatMap(m -> m.getColumnIDs().entrySet().stream())
                            .collect(ImmutableCollectors.toMap());

            String onString = binaryJoinExpression.getFilterCondition()
                    .map(e -> sqlTermSerializer.serialize(e, columnIDs))
                    .map(s -> String.format("ON %s ", s))
                    .orElse("ON 1 = 1 ");

            String sql = formatBinaryJoin(operatorString, left, right, onString);
            return new QuerySerializationImpl(sql, columnIDs);
        }

        protected String formatBinaryJoin(String operatorString, QuerySerialization left, QuerySerialization right, String onString) {
            return String.format("%s\n %s \n%s %s", left.getString(), operatorString, right.getString(), onString);
        }

        @Override
        public QuerySerialization visit(SQLOneTupleDummyQueryExpression sqlOneTupleDummyQueryExpression) {
            String fromString = serializeDummyTable();
            String sqlSubString = String.format("(SELECT 1 %s) tdummy", fromString);
            return new QuerySerializationImpl(sqlSubString, ImmutableMap.of());
        }

        @Override
        public QuerySerialization visit(SQLValuesExpression sqlValuesExpression) {
            ImmutableList<Variable> orderedVariables = sqlValuesExpression.getOrderedVariables();
            ImmutableMap<Variable, QuotedID> variableAliases = createVariableAliases(ImmutableSet.copyOf(orderedVariables));
            // Leaf node
            ImmutableMap<Variable, QualifiedAttributeID> childColumnIDs = ImmutableMap.of();

            String tuplesSerialized = sqlValuesExpression.getValues().stream()
                    .map(tuple -> tuple.stream()
                            .map(constant -> sqlTermSerializer.serialize(constant, childColumnIDs))
                            .collect(Collectors.joining(",", " (", ")")))
                    .collect(Collectors.joining(","));
            RelationID alias = generateFreshViewAlias();
            String internalColumnNames = orderedVariables.stream()
                    .map(variableAliases::get)
                    .map(QuotedID::toString)
                    .collect(Collectors.joining(",", " (", ")"));
            String sql = "(VALUES " + tuplesSerialized + ") AS " + alias + internalColumnNames;

            ImmutableMap<Variable, QualifiedAttributeID> columnIDs = attachRelationAlias(alias, variableAliases);

            return new QuerySerializationImpl(sql, columnIDs);
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


    protected static class DefaultSQLTermSerializer implements SQLTermSerializer {

        private final TermFactory termFactory;

        protected DefaultSQLTermSerializer(TermFactory termFactory) {
            this.termFactory = termFactory;
        }

        @Override
        public String serialize(ImmutableTerm term, ImmutableMap<Variable, QualifiedAttributeID> columnIDs)
                throws SQLSerializationException {
            if (term instanceof Constant) {
                return serializeConstant((Constant)term);
            }
            else if (term instanceof Variable) {
                return Optional.ofNullable(columnIDs.get(term))
                        .map(QualifiedAttributeID::getSQLRendering)
                        .orElseThrow(() -> new SQLSerializationException(String.format(
                                "The variable %s does not appear in the columnIDs", term)));
            }
            /*
             * ImmutableFunctionalTerm with a DBFunctionSymbol
             */
            else {
                return Optional.of(term)
                        .filter(t -> t instanceof ImmutableFunctionalTerm)
                        .map(t -> (ImmutableFunctionalTerm) t)
                        .filter(t -> t.getFunctionSymbol() instanceof DBFunctionSymbol)
                        .map(t -> ((DBFunctionSymbol) t.getFunctionSymbol()).getNativeDBString(
                                t.getTerms(),
                                t2 -> serialize(t2, columnIDs),
                                termFactory))
                        .orElseThrow(() -> new SQLSerializationException("Only DBFunctionSymbols must be provided " +
                                "to a SQLTermSerializer"));
            }
        }

        private String serializeConstant(Constant constant) {
            if (constant.isNull())
                return constant.getValue();
            if (!(constant instanceof DBConstant)) {
                throw new SQLSerializationException(
                        "Only DBConstants or NULLs are expected in sub-tree to be translated into SQL");
            }
            return serializeDBConstant((DBConstant) constant);
        }

        protected String serializeDBConstant(DBConstant constant) {
            DBTermType dbType = constant.getType();

            switch (dbType.getCategory()) {
                case DECIMAL:
                case FLOAT_DOUBLE:
                    // TODO: handle the special case of not-a-number!
                    return castFloatingConstant(constant.getValue(), dbType);
                case INTEGER:
                case BOOLEAN:
                    return constant.getValue();
                case DATE:
                case DATETIME:
                    return serializeDatetimeConstant(constant.getValue(), dbType);
                default:
                    return serializeStringConstant(constant.getValue());
            }
        }

        protected String castFloatingConstant(String value, DBTermType dbType) {
            return String.format("CAST(%s AS %s)", value, dbType.getCastName());
        }

        protected String serializeStringConstant(String constant) {
            // duplicates single quotes, and adds outermost quotes
            return "'" + constant.replace("'", "''") + "'";
        }

        protected String serializeDatetimeConstant(String datetime, DBTermType dbType) {
            return serializeStringConstant(datetime);
        }
    }
}
