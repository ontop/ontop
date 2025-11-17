package it.unibz.inf.ontop.generation.serializer.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.impl.BlackBoxViewDefinition;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.generation.algebra.*;
import it.unibz.inf.ontop.generation.serializer.SQLSerializationException;
import it.unibz.inf.ontop.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.injection.OntopSQLCoreSettings;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class DefaultSelectFromWhereSerializer implements SelectFromWhereSerializer {

    protected final SQLTermSerializer sqlTermSerializer;
    private final String ctePrefix;
    private final boolean useCTEs;


    @Inject
    private DefaultSelectFromWhereSerializer(TermFactory termFactory, OntopSQLCoreSettings settings) {
        this(new DefaultSQLTermSerializer(termFactory), settings);
    }

    protected DefaultSelectFromWhereSerializer(SQLTermSerializer sqlTermSerializer, OntopSQLCoreSettings settings) {
        this.sqlTermSerializer = sqlTermSerializer;
        this.ctePrefix = settings.getOntopCommonTableExpressionsPrefix();
        this.useCTEs = settings.useCommonTableExpressionsForBlackViewsIfSupported();
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

        protected final AtomicInteger viewCounter;
        private final Map<String, RelationID> commonTableExpressions = Maps.newHashMap();

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
                    .map(e -> serializeTerm(e, columnIDs))
                    .map(s -> String.format("WHERE %s\n", s))
                    .orElse("");

            String groupByString = serializeGroupBy(selectFromWhere.getGroupByVariables(), columnIDs);
            String orderByString = serializeOrderBy(selectFromWhere.getSortConditions(),
                    columnIDs,
                    selectFromWhere.getOffset().isPresent() || selectFromWhere.getLimit().isPresent());
            String sliceString = serializeSlice(selectFromWhere.getLimit(), selectFromWhere.getOffset(),
                    selectFromWhere.getSortConditions().isEmpty());

            String sql = String.format(SELECT_FROM_WHERE_MODIFIERS_TEMPLATE, distinctString, projectionString,
                    fromString, whereString, groupByString, orderByString, sliceString);

            // Creates an alias for this SQLExpression and uses it for the projected columns
            RelationID alias = generateFreshViewAlias();
            return new QuerySerializationImpl(sql, attachRelationAlias(alias, variableAliases), fromQuerySerialization.getCTEMap());
        }

        protected RelationID generateFreshViewAlias() {
            return idFactory.createRelationID(VIEW_PREFIX + viewCounter.incrementAndGet());
        }

        protected ImmutableMap<Variable, QualifiedAttributeID> attachRelationAlias(RelationID alias, ImmutableMap<Variable, QuotedID> variableAliases) {
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

        protected final ImmutableMap<Variable, QuotedID> createVariableAliases(ImmutableSet<Variable> variables) {
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
                                             Substitution<? extends ImmutableTerm> substitution,
                                             ImmutableMap<Variable, QualifiedAttributeID> columnIDs) {

            if (projectedVariables.isEmpty())
                return "1 AS uselessVariable";

            return projectedVariables.stream()
                    .map(v -> serializeColumnAlias(
                            serializeTerm(substitution.apply(v), columnIDs),
                            variableAliases.get(v).getSQLRendering()))
                    .collect(Collectors.joining(", "));
        }

        protected final String serializeColumnAlias(String expression, String alias) {
            return expression + " AS " + alias;
        }

        protected final String serializeTerm(ImmutableTerm term, ImmutableMap<Variable, QualifiedAttributeID> allColumnIDs) {
            return sqlTermSerializer.serialize(term, allColumnIDs);
        }

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        protected final String serializeOptionalTerm(String format, Optional<? extends ImmutableTerm> optionalVariable, ImmutableMap<Variable, QualifiedAttributeID> allColumnIDs) {
            return optionalVariable
                    .map(variable -> String.format(format, serializeTerm(variable, allColumnIDs)))
                    .orElse("");
        }

        protected String serializeGroupBy(ImmutableSet<Variable> groupByVariables,
                                          ImmutableMap<Variable, QualifiedAttributeID> columnIDs) {
            if (groupByVariables.isEmpty())
                return "";

            String variableString = groupByVariables.stream()
                    .map(v -> serializeTerm(v, columnIDs))
                    .collect(Collectors.joining(", "));

            return String.format("GROUP BY %s\n", variableString);
        }

        protected String serializeOrderBy(ImmutableList<SQLOrderComparator> sortConditions,
                                        ImmutableMap<Variable, QualifiedAttributeID> columnIDs) {
            if (sortConditions.isEmpty())
                return "";

            String conditionString = sortConditions.stream()
                    .map(c -> serializeOrderByComparator(c, columnIDs))
                    .collect(Collectors.joining(", "));

            return String.format("ORDER BY %s\n", conditionString);
        }

        protected String serializeOrderByComparator(SQLOrderComparator c, ImmutableMap<Variable, QualifiedAttributeID> columnIDs) {
            return serializeTerm(c.getTerm(), columnIDs)
                    + (c.isAscending() ? " NULLS FIRST" : " DESC NULLS LAST");
        }

        /**
         * By default, calls serializeOrderBy without hasOffsetOrLimit. Can be used for specific dialects that
         * have to handle ORDER BY differently if an offset or limit is provided (e.g. SQLServer)
         */
        protected String serializeOrderBy(ImmutableList<SQLOrderComparator> sortConditions,
                                          ImmutableMap<Variable, QualifiedAttributeID> columnIDs, boolean hasOffsetOrLimit) {
            return serializeOrderBy(sortConditions, columnIDs);
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
        protected String serializeSlice(Optional<Long> limit, Optional<Long> offset, boolean noSortCondition) {
            if (!limit.isPresent() && !offset.isPresent())
                return "";

            if (limit.isPresent() && offset.isPresent())
                return serializeLimitOffset(limit.get(), offset.get(), noSortCondition);

            if (limit.isPresent())
                return serializeLimit(limit.get(), noSortCondition);

            return serializeOffset(offset.get(), noSortCondition);
        }

        @Override
        public QuerySerialization visit(SQLTable sqlTable) {
            RelationDefinition relation = sqlTable.getRelationDefinition();
            if (useCTEs
                    && isCTEExpansionOfBlackBoxViewsSupported()
                    && relation instanceof BlackBoxViewDefinition) {
                return serializeRelationAsCTE(relation, sqlTable.getArgumentMap());
            }
            return serializeRelation(relation, sqlTable.getArgumentMap());
        }

        protected boolean isCTEExpansionOfBlackBoxViewsSupported() {
            return true;
        }

        protected final QuerySerialization serializeRelation(RelationDefinition relation, ImmutableMap<Integer, ? extends ImmutableTerm> argumentMap) {
            RelationID alias = generateFreshViewAlias();
            String relationRendering = relation.getAtomPredicate().getName();
            String sql = String.format("%s %s", relationRendering, alias.getSQLRendering());

            return new QuerySerializationImpl(
                    sql,
                    attachRelationAlias(alias, getRelationColumnIDs(relation, argumentMap)),
                    ImmutableMap.of());
        }

        protected final QuerySerialization serializeRelationAsCTE(RelationDefinition relation, ImmutableMap<Integer, ? extends ImmutableTerm> argumentMap) {
            String relationDefinition = relation.getAtomPredicate().getName();
            RelationID cteID = commonTableExpressions.computeIfAbsent(
                    relationDefinition,
                    k -> idFactory.createRelationID(ctePrefix + commonTableExpressions.size()));

            RelationID alias = generateFreshViewAlias();
            String sql = String.format("%s %s", cteID.getSQLRendering(), alias.getSQLRendering());

            return new QuerySerializationImpl(
                    sql,
                    attachRelationAlias(alias, getRelationColumnIDs(relation, argumentMap)),
                    ImmutableMap.of(cteID.getSQLRendering(), relationDefinition));
        }

        protected final ImmutableMap<Variable, QuotedID> getRelationColumnIDs(RelationDefinition relation, ImmutableMap<Integer, ? extends ImmutableTerm> argumentMap) {
            return argumentMap.entrySet().stream()
                    .collect(ImmutableCollectors.toMap(
                            // Ground terms must have been already removed from atoms
                            e -> (Variable) e.getValue(),
                            e -> relation.getAttribute(e.getKey() + 1).getID()));
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

            return new QuerySerializationImpl(sql, columnIDs, combineCTEs(querySerializationList));
        }

        @Override
        public QuerySerialization visit(SQLUnionExpression sqlUnionExpression) {
            ImmutableList<QuerySerialization> querySerializationList = sqlUnionExpression.getSubExpressions().stream()
                    .map(e -> e.acceptVisitor(this))
                    .collect(ImmutableCollectors.toList());

            RelationID alias = generateFreshViewAlias();
            String sql = String.format("(%s) %s", querySerializationList.stream()
                    .map(QuerySerialization::getString)
                    .map(s -> "(" + s + ")")
                    .collect(Collectors.joining("UNION ALL \n")), alias.getSQLRendering());

            return new QuerySerializationImpl(
                    sql,
                    replaceRelationAlias(alias, querySerializationList.get(0).getColumnIDs()),
                    combineCTEs(querySerializationList));
        }

        //this function is required in case at least one of the children is
        // SelectFromWhereWithModifiers expression
        protected QuerySerialization getSQLSerializationForChild(SQLExpression expression) {
            if (expression instanceof SelectFromWhereWithModifiers) {
                QuerySerialization serialization = expression.acceptVisitor(this);
                RelationID alias = generateFreshViewAlias();
                String sql = String.format("(%s) %s", serialization.getString(), alias.getSQLRendering());
                return new QuerySerializationImpl(
                        sql,
                        replaceRelationAlias(alias, serialization.getColumnIDs()),
                        serialization.getCTEMap());
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
         * Indeed, since a join expression with a ON is always "CHILD_1 SOME_JOIN CHILD_2 ON COND",
         * the decomposition is unambiguous just following this pattern.
         *
         * For instance, "T1 LEFT JOIN T2 INNER JOIN T3 ON 1=1 ON 2=2"
         * is clearly equivalent to "T1 LEFT JOIN (T2 INNER JOIN T3)"
         * as the latest ON condition ("ON 2=2") can only be attached to the left join, which means that "T2 INNER JOIN T3 ON 1=1"
         * is the right child of the left join.
         *
         */
        protected QuerySerialization visit(SQLBinaryJoinExpression binaryJoinExpression, String operatorString) {
            QuerySerialization left = getSQLSerializationForChild(binaryJoinExpression.getLeft());
            QuerySerialization right = getSQLSerializationForChild(binaryJoinExpression.getRight());
            ImmutableList<QuerySerialization> subQuerySerializations = ImmutableList.of(left, right);

            ImmutableMap<Variable, QualifiedAttributeID> columnIDs = subQuerySerializations.stream()
                            .flatMap(m -> m.getColumnIDs().entrySet().stream())
                            .collect(ImmutableCollectors.toMap());

            String onString = binaryJoinExpression.getFilterCondition()
                    .map(e -> serializeTerm(e, columnIDs))
                    .map(s -> String.format("ON %s ", s))
                    .orElse("ON 1 = 1 ");

            String sql = formatBinaryJoin(operatorString, left, right, onString);
            return new QuerySerializationImpl(sql, columnIDs, combineCTEs(subQuerySerializations));
        }

        protected String formatBinaryJoin(String operatorString, QuerySerialization left, QuerySerialization right, String onString) {
            return String.format("%s\n %s \n%s %s", left.getString(), operatorString, right.getString(), onString);
        }

        @Override
        public QuerySerialization visit(SQLOneTupleDummyQueryExpression sqlOneTupleDummyQueryExpression) {
            String fromString = serializeDummyTable();
            String sqlSubString = String.format("(SELECT 1 %s) tdummy", fromString);
            return new QuerySerializationImpl(sqlSubString, ImmutableMap.of(), ImmutableMap.of());
        }

        private final ImmutableMap<Variable, QualifiedAttributeID> emptyColumnIDs = ImmutableMap.of();

        protected String serializeValuesEntry(Constant constant) {
            return sqlTermSerializer.serialize(constant, emptyColumnIDs);
        }

        protected final String serializeValuesEntries(ImmutableList<ImmutableList<Constant>> values) {
            return values.stream()
                    .map(tuple -> tuple.stream()
                            .map(this::serializeValuesEntry)
                            .collect(Collectors.joining(",", " (", ")")))
                    .collect(Collectors.joining(","));
        }

        protected final String serializeValuesColumnNames(ImmutableList<Variable> orderedVariables, ImmutableMap<Variable, QuotedID> variableAliases, Function<QuotedID, String> serializeId) {
            return orderedVariables.stream()
                    .map(variableAliases::get)
                    .map(serializeId)
                    .collect(Collectors.joining(",", " (", ")"));
        }

        @Override
        public QuerySerialization visit(SQLValuesExpression sqlValuesExpression) {
            ImmutableList<Variable> orderedVariables = sqlValuesExpression.getOrderedVariables();
            ImmutableMap<Variable, QuotedID> variableAliases = createVariableAliases(ImmutableSet.copyOf(orderedVariables));

            RelationID alias = generateFreshViewAlias();
            String sql = String.format("(VALUES %s) AS %s%s",
                    serializeValuesEntries(sqlValuesExpression.getValues()),
                    alias,
                    serializeValuesColumnNames(orderedVariables, variableAliases, QuotedID::getSQLRendering));

            ImmutableMap<Variable, QualifiedAttributeID> columnIDs = attachRelationAlias(alias, variableAliases);
            return new QuerySerializationImpl(sql, columnIDs, ImmutableMap.of());
        }

        @Override
        public QuerySerialization visit(SQLFlattenExpression sqlFlattenExpression) {
            QuerySerialization subQuerySerialization = getSQLSerializationForChild(sqlFlattenExpression.getSubExpression());
            ImmutableMap<Variable, QualifiedAttributeID> allColumnIDs = buildFlattenColumIDMap(
                    sqlFlattenExpression,
                    subQuerySerialization);

            Variable flattenedVar = sqlFlattenExpression.getFlattenedVar();
            Variable outputVar = sqlFlattenExpression.getOutputVar();
            DBTermType flattenedType = sqlFlattenExpression.getFlattenedType();
            Optional<Variable> indexVar = sqlFlattenExpression.getIndexVar();

            return serializeFlatten(sqlFlattenExpression, flattenedVar, outputVar, indexVar, flattenedType, allColumnIDs, subQuerySerialization);
        }

        protected QuerySerialization serializeFlatten(SQLFlattenExpression sqlFlattenExpression, Variable flattenedVar,
                                                      Variable outputVar, Optional<Variable> indexVar, DBTermType flattenedType,
                                                      ImmutableMap<Variable, QualifiedAttributeID> allColumnIDs, QuerySerialization subQuerySerialization) {
            throw new UnsupportedOperationException("Nested data support unavailable for this DBMS");
        }

        protected final ImmutableMap<Variable, QualifiedAttributeID> buildFlattenColumIDMap(SQLFlattenExpression sqlFlattenExpression,
                                                                                    QuerySerialization subQuerySerialization) {
            ImmutableSet<Variable> freshVariables = sqlFlattenExpression.getIndexVar().isPresent()
                    ? ImmutableSet.of(sqlFlattenExpression.getOutputVar(), sqlFlattenExpression.getIndexVar().get())
                    : ImmutableSet.of(sqlFlattenExpression.getOutputVar());

            ImmutableMap<Variable, QualifiedAttributeID> freshVariableAliases = createVariableAliases(freshVariables).entrySet().stream()
                    .collect(ImmutableCollectors.toMap(
                            Map.Entry::getKey,
                            e -> new QualifiedAttributeID(null, e.getValue())));

            return ImmutableMap.<Variable, QualifiedAttributeID>builder()
                    .putAll(freshVariableAliases)
                    .putAll(subQuerySerialization.getColumnIDs())
                    .build();
        }

        protected final ImmutableMap<Variable, QualifiedAttributeID> getFlattenAllColumnIDs(Variable flattenedVar, ImmutableMap<Variable, QualifiedAttributeID> allColumnIDs) {
            return allColumnIDs.entrySet().stream()
                    .filter(e -> e.getKey() != flattenedVar)
                    .collect(ImmutableCollectors.toMap());
        }

        protected final QuerySerialization serializeFlattenAsJoin(Variable flattenedVar, String expression, ImmutableMap<Variable, QualifiedAttributeID> allColumnIDs, QuerySerialization subQuerySerialization) {
            String string = String.format(getFlattenJoinTemplate(),
                    subQuerySerialization.getString(),
                    expression,
                    generateFreshViewAlias().getSQLRendering());

            return new QuerySerializationImpl(
                    string,
                    getFlattenAllColumnIDs(flattenedVar, allColumnIDs),
                    subQuerySerialization.getCTEMap());
        }

        protected String getFlattenJoinTemplate() {
            return "%s CROSS JOIN %s %s";
        }

        protected final QuerySerialization serializeFlattenAsSubQuery(Variable flattenedVar, ImmutableMap<Variable, QualifiedAttributeID> allColumnIDs,
                                                                      QuerySerialization subQuerySerialization, Stream<String> projectionExtensions) {
            RelationID alias = generateFreshViewAlias();
            var variableAliases = replaceRelationAlias(alias, getFlattenAllColumnIDs(flattenedVar, allColumnIDs));

            var aliasFactory = createAttributeAliasFactory();
            String projection = Stream.concat(
                            subQuerySerialization.getColumnIDs().keySet().stream()
                                    .filter(variableAliases.keySet()::contains)
                                    .map(v -> serializeColumnAlias(
                                            serializeTerm(v, subQuerySerialization.getColumnIDs()),
                                            aliasFactory.createAttributeAlias(v.getName()).getSQLRendering())),
                            projectionExtensions)
                    .collect(Collectors.joining(", "));

            String string = String.format(getFlattenSubQueryTemplate(),
                    projection,
                    subQuerySerialization.getString(),
                    alias.getSQLRendering());

            return new QuerySerializationImpl(
                    string,
                    variableAliases,
                    subQuerySerialization.getCTEMap());
        }

        protected String getFlattenSubQueryTemplate() {
            return "(SELECT %s FROM %s) %s";
        }
    }


    protected static class QuerySerializationImpl implements QuerySerialization {

        private final String preambleFreeString;
        private final ImmutableMap<Variable, QualifiedAttributeID> columnIDs;
        private final ImmutableMap<String, String> cteMap;

        protected QuerySerializationImpl(String preambleFreeString, ImmutableMap<Variable, QualifiedAttributeID> columnIDs, ImmutableMap<String, String> cteMap) {
            this.preambleFreeString = preambleFreeString;
            this.columnIDs = columnIDs;
            this.cteMap = cteMap;
        }

        @Override
        public String getStringWithPreamble() {
            return cteMap.isEmpty()
                    ? preambleFreeString
                    : cteMap.entrySet().stream()
                        .map(e -> e.getKey() + " AS (" + e.getValue() + ")")
                        .collect(Collectors.joining(",\n", "WITH\n", "\n"))
                    + preambleFreeString;
        }

        @Override
        public ImmutableMap<String, String> getCTEMap() {
            return cteMap;
        }

        @Override
        public String getString() {
            return preambleFreeString;
        }

        @Override
        public ImmutableMap<Variable, QualifiedAttributeID> getColumnIDs() {
            return columnIDs;
        }
    }


    protected ImmutableMap<String, String> combineCTEs(QuerySerialization subQuerySerialization, ImmutableMap<String, String> cteStrings) {
        return combineCTEs(Stream.of(subQuerySerialization.getCTEMap(), cteStrings));
    }

    protected ImmutableMap<String, String> combineCTEs(ImmutableList<QuerySerialization> subQuerySerializations) {
        return combineCTEs(subQuerySerializations.stream().map(QuerySerialization::getCTEMap));
    }

    private ImmutableMap<String, String> combineCTEs(Stream<ImmutableMap<String, String>> cteMapStream) {
        return cteMapStream
                .map(ImmutableMap::entrySet)
                .flatMap(ImmutableSet::stream)
                .collect(toOrderedImmutableMap());
    }

    private static <K, U> Collector<Map.Entry<K, U>, ? ,ImmutableMap<K,U>> toOrderedImmutableMap() {
        BinaryOperator<U> mergeFunction = (v1, v2) -> {
            if (!v1.equals(v2))
                throw new MinorOntopInternalBugException("incompatible CTE definitions");
            return v1;
        };

        BinaryOperator<Map<K,U>> mapMerger = (m1, m2) -> {
            for (Map.Entry<K,U> e : m2.entrySet())
                m1.merge(e.getKey(), e.getValue(), mergeFunction);
            return m1;
        };

        return Collector.of(
                Maps::newLinkedHashMap, // preserves the order!
                (m, e) -> m.merge(e.getKey(), e.getValue(), mergeFunction),
                mapMerger,
                ImmutableMap::copyOf);
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
                    return constant.getValue();
                case BOOLEAN:
                    return serializeBooleanConstant(constant);
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

        protected String serializeBooleanConstant(DBConstant booleanConstant) {
            return booleanConstant.getValue();
        }
    }
}
