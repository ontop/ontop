package it.unibz.inf.ontop.generation.serializer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
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
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.AbstractMap;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class SnowflakeSelectFromWhereSerializer extends DefaultSelectFromWhereSerializer implements SelectFromWhereSerializer {

    @Inject
    private SnowflakeSelectFromWhereSerializer(TermFactory termFactory) {
        super(new DefaultSQLTermSerializer(termFactory));
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
                     * Variables in the VALUES block needs to be without lower case!
                     * (limitation of Snowflake)
                     */
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

                        RelationID valuesAlias = generateFreshViewAlias();
                        // The values alias will be wrapped
                        RelationID wrapperAlias = generateFreshViewAlias();

                        String internalColumnNames = orderedVariables.stream()
                                .map(variableAliases::get)
                                // No quoting (lower-case are not tolerated here by Snowflake)
                                .map(QuotedID::getName)
                                .collect(Collectors.joining(",", " (", ")"));
                        String valuesSql = "(VALUES " + tuplesSerialized + ") AS " + valuesAlias + internalColumnNames;

                        String renamingProjection = orderedVariables.stream()
                                .map(variableAliases::get)
                                .map(quotedID -> String.format("%s.%s AS %s", valuesAlias.getSQLRendering(),
                                        quotedID.getName(), quotedID.getSQLRendering()))
                                .collect(Collectors.joining(","));

                        String sql = "(SELECT " + renamingProjection + " FROM " + valuesSql + ") AS " + wrapperAlias;

                        ImmutableMap<Variable, QualifiedAttributeID> columnIDs = attachRelationAlias(wrapperAlias, variableAliases);

                        return new QuerySerializationImpl(sql, columnIDs);

                    }

                    @Override
                    protected QuerySerialization serializeFlatten(SQLFlattenExpression sqlFlattenExpression, Variable flattenedVar, Variable outputVar, Optional<Variable> indexVar, DBTermType flattenedType, ImmutableMap<Variable, QualifiedAttributeID> allColumnIDs, QuerySerialization subQuerySerialization) {
                        //We build the query string of the form SELECT <variables> FROM <subquery>, LATERAL FLATTEN(<flattenedVariable>) AS <viewName>(dummy, dummy, dummy, {dummy|<indexVar>}, <outputVar>)
                        StringBuilder builder = new StringBuilder();

                        //Make sure the dummy variables we use for the remaining FLATTEN outputs are not already in use.
                        String dummy = "t";
                        while (true) {
                            dummy = "_" + dummy;
                            String dummyTemp = dummy;
                            if(allColumnIDs.values().stream().noneMatch(a -> a.getAttribute().getName().startsWith(dummyTemp)))
                                break;
                        }

                        //Quotation marks are not supported in these aliases, so we use `getName()` instead of `getSQLRendering()`.
                        builder.append(
                                String.format(
                                        "%s, LATERAL FLATTEN(%s) AS %s(%s, %s, %s, %s, %s, %s)",
                                        subQuerySerialization.getString(),
                                        allColumnIDs.get(flattenedVar).getSQLRendering(),
                                        generateFreshViewAlias().getSQLRendering(),
                                        dummy,
                                        dummy,
                                        dummy,
                                        indexVar.map(v -> allColumnIDs.get(v).getAttribute().getName())
                                                .orElse("dummyVariable"),
                                        allColumnIDs.get(outputVar).getAttribute().getName(),
                                        dummy
                                ));

                        //We have to convert the index and output variables to upper case, otherwise dropping the quotation marks will not work.
                        return new QuerySerializationImpl(
                                builder.toString(),
                                allColumnIDs.entrySet().stream()
                                        .filter(e -> e.getKey() != flattenedVar)
                                        .map(e -> (e.getKey() != outputVar && e.getKey() != indexVar.orElse(null)) ?
                                                e :
                                                new AbstractMap.SimpleEntry<>(
                                                        e.getKey(),
                                                        new QualifiedAttributeID(e.getValue().getRelation(), idFactory.createAttributeID(e.getValue().getAttribute().getName().toUpperCase()))
                                                ))
                                        .collect(ImmutableCollectors.toMap())
                        );
                    }

                });
    }
}
