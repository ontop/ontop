package it.unibz.inf.ontop.answering.reformulation.generation.serializer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SQLRelationVisitor;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SQLSerializedQuery;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SQLTable;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.answering.reformulation.generation.dialect.SQLDialectAdapter;
import it.unibz.inf.ontop.answering.reformulation.generation.serializer.SQLTermSerializer;
import it.unibz.inf.ontop.answering.reformulation.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.iq.node.OrderByNode;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
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
        private static final String SELECT_FROM_WHERE_MODIFIERS_TEMPLATE = "SELECT %s%s\nFROM (%s)\n%s%s%s";
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

            QuerySerialization fromQuerySerialization = selectFromWhere.getFromSQLExpression().acceptVisitor(this);

            // Assumes that from expressions all use different variables
            ImmutableMap<Variable, QualifiedAttributeID> fromColumnIDs = fromQuerySerialization.getColumnIDs();

            ImmutableMap<Variable, QualifiedAttributeID> projectedColumnMap = extractProjectionColumnMap(
                    selectFromWhere.getProjectedVariables(), fromColumnIDs);

            String distinctString = selectFromWhere.isDistinct() ? "DISTINCT " : "";

            String projectionString = serializeProjection(selectFromWhere.getProjectedVariables(),
                    projectedColumnMap, selectFromWhere.getSubstitution(), fromColumnIDs);

            String fromString = fromQuerySerialization.getString();

            String whereString = selectFromWhere.getWhereExpression()
                    .map(e -> sqlTermSerializer.serialize(e, fromColumnIDs))
                    .map(s -> String.format("WHERE %s\n", s))
                    .orElse("");

            String orderByString = serializeOrderBy(selectFromWhere.getSortConditions(), fromColumnIDs);
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
        private ImmutableMap<Variable, QualifiedAttributeID> extractProjectionColumnMap(
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
                                return fromColumnMap.get(v);

                                String newColumnName = dialectAdapter.nameTopVariable(v.getName(), quotedColumnNames);
                                quotedColumnNames.add(newColumnName);
                                return createQualifiedAttributeId(null, newColumnName);
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
                            .map(s -> s + " AS " + projectedColumnMap.get(v))
                            .orElseGet(() -> projectedColumnMap.get(v).getSQLRendering()))
                    .collect(Collectors.joining(", "));
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

            RelationID alias = generateFreshViewAlias();
            ImmutableMap<Variable, QualifiedAttributeID> columnIDs = sqlSerializedQuery.getColumnNames().entrySet().stream()
                    .collect(ImmutableCollectors.toMap(
                            Map.Entry::getKey,
                            e -> createQualifiedAttributeId(alias, e.getValue())
                    ));

            return new QuerySerializationImpl(sqlSerializedQuery.getSQLString(), columnIDs);
        }

        @Override
        public QuerySerialization visit(SQLTable sqlTable) {
            DataAtom<RelationPredicate> atom = sqlTable.getAtom();

            RelationID originalRelationId = atom.getPredicate().getRelationDefinition().getID();
            RelationID aliasId = generateFreshViewAlias();

            return new QuerySerializationImpl(originalRelationId.getSQLRendering(),
                    atom.getArguments().stream()
                            // Ground terms must have been already removed from atoms
                            .map(a -> (Variable)a)
                            .collect(ImmutableCollectors.toMap(
                                    v -> v,
                                    v -> createQualifiedAttributeId(aliasId, v.getName())
                            )));
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
