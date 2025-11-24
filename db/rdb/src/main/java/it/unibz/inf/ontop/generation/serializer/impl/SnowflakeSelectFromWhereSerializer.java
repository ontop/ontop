package it.unibz.inf.ontop.generation.serializer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.generation.algebra.SQLFlattenExpression;
import it.unibz.inf.ontop.generation.algebra.SQLValuesExpression;
import it.unibz.inf.ontop.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.injection.OntopSQLCoreSettings;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class SnowflakeSelectFromWhereSerializer extends DefaultSelectFromWhereSerializer implements SelectFromWhereSerializer {

    @Inject
    private SnowflakeSelectFromWhereSerializer(TermFactory termFactory, OntopSQLCoreSettings settings) {
        super(new DefaultSQLTermSerializer(termFactory), settings);
    }

    @Override
    public QuerySerialization serialize(SelectFromWhereWithModifiers selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(
                new DefaultRelationVisitingSerializer(dbParameters.getQuotedIDFactory()) {

                    @Override
                    protected String serializeLimitOffset(long limit, long offset, boolean noSortCondition) {
                        return String.format("LIMIT %d OFFSET %d", limit, offset);
                    }

                    @Override
                    protected String serializeOffset(long offset, boolean noSortCondition) {
                        return String.format("LIMIT NULL OFFSET %d", offset);
                    }

                    /**
                     * Snowflake limitation: variables in the VALUES block needs to be without lower case!
                     */
                    @Override
                    public QuerySerialization visit(SQLValuesExpression sqlValuesExpression) {
                        ImmutableList<Variable> orderedVariables = sqlValuesExpression.getOrderedVariables();
                        ImmutableMap<Variable, QuotedID> variableAliases = createVariableAliases(ImmutableSet.copyOf(orderedVariables));

                        RelationID valuesAlias = generateFreshViewAlias();

                        String renamingProjection = orderedVariables.stream()
                                .map(variableAliases::get)
                                .map(quotedID -> serializeColumnAlias(
                                        String.format("%s.%s", valuesAlias.getSQLRendering(), quotedID.getName()),
                                        quotedID.getSQLRendering()))
                                .collect(Collectors.joining(","));

                        RelationID wrapperAlias = generateFreshViewAlias();
                        String sql = String.format("(SELECT %s FROM (VALUES %s) AS %s%s) AS %s",
                                renamingProjection,
                                serializeValuesEntries(sqlValuesExpression.getValues()),
                                valuesAlias,
                                // No quoting (lower-case are not tolerated here by Snowflake)
                                serializeValuesColumnNames(orderedVariables, variableAliases, QuotedID::getName),
                                wrapperAlias);

                        ImmutableMap<Variable, QualifiedAttributeID> columnIDs = attachRelationAlias(wrapperAlias, variableAliases);
                        return new QuerySerializationImpl(sql, columnIDs, ImmutableMap.of());
                    }

                    @Override
                    protected QuerySerialization serializeFlatten(SQLFlattenExpression sqlFlattenExpression, Variable flattenedVar, Variable outputVar, Optional<Variable> indexVar, DBTermType flattenedType, ImmutableMap<Variable, QualifiedAttributeID> allColumnIDs, QuerySerialization subQuerySerialization) {
                        //We build the query string of the form SELECT <variables> FROM <subquery>, LATERAL FLATTEN(<flattenedVariable>) AS <viewName>(dummy, dummy, dummy, {dummy|<indexVar>}, <outputVar>)

                        //Make sure the dummy variables we use for the remaining FLATTEN outputs are not already in use.
                        String dummy = "t";
                        while (true) {
                            dummy = "_" + dummy;
                            String dummyTemp = dummy;
                            if (allColumnIDs.values().stream().noneMatch(a -> a.getAttribute().getName().startsWith(dummyTemp)))
                                break;
                        }

                        //Quotation marks are not supported in these aliases, so we use `getName()` instead of `getSQLRendering()`.
                        String string = String.format(
                                "%s, LATERAL FLATTEN(%s) AS %s(%s, %s, %s, %s, %s, %s)",
                                subQuerySerialization.getString(),
                                serializeTerm(flattenedVar, allColumnIDs),
                                generateFreshViewAlias().getSQLRendering(),
                                dummy,
                                dummy,
                                dummy,
                                indexVar.map(v -> allColumnIDs.get(v).getAttribute().getName())
                                        .orElse("dummyVariable"),
                                allColumnIDs.get(outputVar).getAttribute().getName(),
                                dummy);

                        //We have to convert the index and output variables to upper case, otherwise dropping the quotation marks will not work.
                        var newColumnIDs = allColumnIDs.entrySet().stream()
                                .filter(e -> e.getKey() != flattenedVar)
                                .map(e -> (e.getKey() != outputVar && e.getKey() != indexVar.orElse(null))
                                        ? e
                                        : Maps.immutableEntry(
                                        e.getKey(),
                                        new QualifiedAttributeID(e.getValue().getRelation(), idFactory.createAttributeID(e.getValue().getAttribute().getName().toUpperCase()))))
                                .collect(ImmutableCollectors.toMap());

                        return new QuerySerializationImpl(string, newColumnIDs, subQuerySerialization.getCTEMap());
                    }
                });
    }
}
