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
import it.unibz.inf.ontop.generation.serializer.SQLSerializationException;
import it.unibz.inf.ontop.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.GenericDBTermType;
import it.unibz.inf.ontop.model.type.impl.ArrayDBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static it.unibz.inf.ontop.model.type.impl.PostgreSQLDBTypeFactory.*;

@Singleton
public class TrinoSelectFromWhereSerializer extends DefaultSelectFromWhereSerializer implements SelectFromWhereSerializer {

    @Inject
    private TrinoSelectFromWhereSerializer(TermFactory termFactory) {
        super(new DefaultSQLTermSerializer(termFactory) {
            @Override
            protected String serializeDatetimeConstant(String datetime, DBTermType dbType) {
                return String.format("TIMESTAMP %s", serializeStringConstant(datetime));
            }
        });
    }

    @Override
    public QuerySerialization serialize(SelectFromWhereWithModifiers selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(
                new DefaultRelationVisitingSerializer(dbParameters.getQuotedIDFactory()) {

                    private static final String VIEW_PREFIX = "r";

                    @Override
                    protected String serializeLimitOffset(long limit, long offset, boolean noSortCondition) {
                        return String.format("OFFSET %d LIMIT %d", offset, limit);
                    }

                    @Override
                    protected String serializeOffset(long offset, boolean noSortCondition) {
                        return String.format("OFFSET %d", offset);
                    }

                    @Override
                    protected RelationID generateFreshViewAlias() {
                        return idFactory.createRelationID(VIEW_PREFIX + viewCounter.incrementAndGet());
                    }

                    /**
                     * Generate a new variable name as an intermediate term for the Array unnest operation.
                     */
                    private QuotedID generateIntermediateVariable(String outputVarName, ImmutableSet<Variable> existingVariables) {
                        int index = 1;
                        while (true) {
                            String newVarName = outputVarName + "_intermediate" + index;
                            if(existingVariables.stream()
                                    .noneMatch(v -> v.getName().equals(newVarName))) {
                                return createAttributeAliasFactory().createAttributeAlias(newVarName);
                            }
                            index++;
                        }
                    }

                    @Override
                    public QuerySerialization visit(SQLFlattenExpression sqlFlattenExpression) {

                        QuerySerialization subQuerySerialization = getSQLSerializationForChild(sqlFlattenExpression.getSubExpression());
                        ImmutableMap<Variable, QualifiedAttributeID> allColumnIDs = buildFlattenColumIDMap(
                                sqlFlattenExpression,
                                subQuerySerialization
                        );

                        Variable flattenedVar = sqlFlattenExpression.getFlattenedVar();
                        Variable outputVar = sqlFlattenExpression.getOutputVar();
                        Optional<Variable> indexVar = sqlFlattenExpression.getIndexVar();

                        //We now build the query string of the form SELECT <variables> FROM <subquery> CROSS JOIN UNNEST(<flattenedVariable>) WITH ORDINALITY AS <names>
                        StringBuilder builder = new StringBuilder();

                        builder.append(
                                String.format(
                                        "%s CROSS JOIN UNNEST(%s) ",
                                        subQuerySerialization.getString(),
                                        allColumnIDs.get(flattenedVar).getSQLRendering()
                                ));
                        indexVar.ifPresent( v -> builder.append(" WITH ORDINALITY "));


                        builder.append(
                                String.format(
                                        "AS %s",
                                        getOutputVarsRendering(outputVar, indexVar, allColumnIDs)
                                )
                        );

                        return new QuerySerializationImpl(
                                builder.toString(),
                                allColumnIDs.entrySet().stream()
                                        .filter(e -> e.getKey() != flattenedVar)
                                        .collect(ImmutableCollectors.toMap())
                        );
                    }

                    private Object getOutputVarsRendering(Variable outputVar, Optional<Variable> indexVar, ImmutableMap<Variable, QualifiedAttributeID> allColumnIDs) {
                        String outputVarString = allColumnIDs.get(outputVar).getSQLRendering();
                        return getOutputVarsRendering(outputVarString, indexVar, allColumnIDs, generateFreshViewAlias());
                    }

                    private Object getOutputVarsRendering(String outputVarString, Optional<Variable> indexVar, ImmutableMap<Variable, QualifiedAttributeID> allColumnIDs, RelationID viewAlias) {
                        return String.format(
                                        "%s(%s%s)",
                                        viewAlias.getSQLRendering(),
                                        outputVarString,
                                        indexVar.isPresent() ? ", " + allColumnIDs.get(indexVar.get()).getSQLRendering() : "");
                    }

                    private ImmutableMap<Variable, QualifiedAttributeID> buildFlattenColumIDMap(SQLFlattenExpression sqlFlattenExpression,
                                                                                                QuerySerialization subQuerySerialization) {


                        ImmutableMap<Variable, QualifiedAttributeID> freshVariableAliases = createVariableAliases(getFreshVariables(sqlFlattenExpression)).entrySet().stream()
                                .collect(ImmutableCollectors.toMap(
                                        Map.Entry::getKey,
                                        e -> new QualifiedAttributeID(null, e.getValue())
                                ));

                        return ImmutableMap.<Variable, QualifiedAttributeID>builder()
                                .putAll(freshVariableAliases)
                                .putAll(subQuerySerialization.getColumnIDs())
                                .build();
                    }

                    private ImmutableSet<Variable> getFreshVariables(SQLFlattenExpression sqlFlattenExpression) {
                        ImmutableSet.Builder<Variable> builder = ImmutableSet.builder();
                        builder.add(sqlFlattenExpression.getOutputVar());
                        sqlFlattenExpression.getIndexVar().ifPresent(builder::add);
                        return builder.build();
                    }

                    private String getFlattenFunctionSymbolString(DBTermType dbType) {
                        DBTypeFactory dbTypeFactory = dbParameters.getDBTypeFactory();

                        if (dbTypeFactory.getDBTermType(JSON_STR).equals(dbType)) {
                            return "json_array_elements(%s)";
                        }
                        if (dbTypeFactory.getDBTermType(JSONB_STR).equals(dbType)) {
                            return "jsonb_array_elements(%s)";
                        }
                        if (dbType.getCategory() == DBTermType.Category.ARRAY) {
                            GenericDBTermType genericDbType = (GenericDBTermType) dbType;
                            //When it is a multi-dimensional array, we cannot use unnest, because it would flatten all levels at once.
                            if(genericDbType.getGenericArguments().get(0).getCategory() == DBTermType.Category.ARRAY) {
                                return "jsonb_array_elements(to_jsonb(%s))";
                            } else
                                return "unnest(%s)";
                        }

                        throw new SQLSerializationException("Unsupported nested type for flattening: " + dbType.getName());
                    }
                });
    }
}
