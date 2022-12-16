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
import it.unibz.inf.ontop.generation.algebra.SQLValuesExpression;
import it.unibz.inf.ontop.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;

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
                });
    }
}
