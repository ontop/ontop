package it.unibz.inf.ontop.generation.serializer.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.generation.algebra.SQLFlattenExpression;
import it.unibz.inf.ontop.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.generation.serializer.SQLSerializationException;
import it.unibz.inf.ontop.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.impl.NullIgnoringDBGroupConcatFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class DremioSelectFromWhereSerializer extends DefaultSelectFromWhereSerializer implements SelectFromWhereSerializer {

    @Inject
    private DremioSelectFromWhereSerializer(TermFactory termFactory) {
        super(new DefaultSQLTermSerializer(termFactory) {
            @Override
            public String serialize(ImmutableTerm term, ImmutableMap<Variable, QualifiedAttributeID> columnIDs) {
                return super.serialize(term,columnIDs);
            }

            @Override
            protected String serializeDatetimeConstant(String datetime, DBTermType dbType) {
                return String.format("CAST(%s AS %s)", serializeStringConstant(datetime), dbType.getCastName());
            }
        });
    }

    @Override
    public QuerySerialization serialize(SelectFromWhereWithModifiers selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(
                new DefaultRelationVisitingSerializer(dbParameters.getQuotedIDFactory()) {
                    @Override
                    protected String serializeLimitOffset(long limit, long offset, boolean noSortCondition) {
                        return String.format("LIMIT %d OFFSET %d", limit, offset);
                    }

                    //                    @Override
//                    protected String serializeProjection(ImmutableSortedSet<Variable> projectedVariables,
//                                                         ImmutableMap<Variable, QuotedID> variableAliases,
//                                                         ImmutableSubstitution<? extends ImmutableTerm> substitution,
//                                                         ImmutableMap<Variable, QualifiedAttributeID> columnIDs) {
//
//                        if (projectedVariables.isEmpty())
//                            return "1 AS uselessVariable";
//
//                        return projectedVariables.stream()
//                                .map(v -> serializeDef(
//                                        v,
//                                        Optional.ofNullable((ImmutableTerm)substitution.get(v)).orElse(v),
//                                        columnIDs,
//                                        variableAliases
//                                ))
//                                .collect(Collectors.joining(", "));
//                    }

//                    private String serializeDef(Variable v, ImmutableTerm term, ImmutableMap<Variable, QualifiedAttributeID> columnIDs, ImmutableMap<Variable, QuotedID> variableAliases) {
//                        return serializeValue(term, columnIDs) + " AS " + variableAliases.get(v).getSQLRendering();
//                    }
//
//                    private String serializeValue(ImmutableTerm term, ImmutableMap<Variable, QualifiedAttributeID> columnIDs) {
//                        return term.isNull()?
//                                castNull(term.inferType()):
//                                sqlTermSerializer.serialize(
//                                    term,
//                                    columnIDs
//                                );
//                    }
//
//                    private String castNull(Optional<TermTypeInference> inferType) {
//                            return String.format(
//                                    "CAST(NULL AS %s)",
//                                    inferType.orElseThrow(
//                                            () ->new SQLSerializationException("a type is expected")));
//                    }


                    //Taken from postgres implementation
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

                    //Taken from postgres implementation
                    private ImmutableSet<Variable> getFreshVariables(SQLFlattenExpression sqlFlattenExpression) {
                        ImmutableSet.Builder<Variable> builder = ImmutableSet.builder();
                        builder.add(sqlFlattenExpression.getOutputVar());
                        sqlFlattenExpression.getIndexVar().ifPresent(builder::add);
                        return builder.build();
                    }

                    //Taken from spark implementation
                    @Override
                    public QuerySerialization visit(SQLFlattenExpression sqlFlattenExpression) {
                        QuerySerialization subQuerySerialization = getSQLSerializationForChild(sqlFlattenExpression.getSubExpression());
                        ImmutableMap<Variable, QualifiedAttributeID> allColumnIDs = buildFlattenColumIDMap(
                                sqlFlattenExpression,
                                subQuerySerialization
                        );

                        Variable flattenedVar = sqlFlattenExpression.getFlattenedVar();
                        Variable outputVar = sqlFlattenExpression.getOutputVar();
                        DBTermType flattenedType = sqlFlattenExpression.getFlattenedType();
                        Optional<Variable> indexVar = sqlFlattenExpression.getIndexVar();
                        StringBuilder builder = new StringBuilder();
                        if(indexVar.isPresent()) {
                            throw new SQLSerializationException("Dremio does not support FLATTEN with position arguments.");
                        }

                        //We express the flatten call as a `SELECT *, FLATTEN({array}) FROM child.

                        //EXPLODE only works on ARRAY<T> types, so we first transform the JSON-array into an ARRAY<STRING> if it is not already one
                        var expression = flattenedType.getCategory() == DBTermType.Category.ARRAY
                                ? allColumnIDs.get(flattenedVar).getSQLRendering()
                                : String.format("CONVERT_FROM(%s, 'json')", allColumnIDs.get(flattenedVar).getSQLRendering());

                        //We compute an alias for the sub-query, and new aliases for each projected variable.
                        var alias = this.generateFreshViewAlias().getSQLRendering();
                        var variableAliases = allColumnIDs.entrySet().stream()
                                .filter(e -> e.getKey() != flattenedVar)
                                .collect(ImmutableCollectors.toMap(
                                        v -> v.getKey(),
                                        v -> new QualifiedAttributeID(idFactory.createRelationID(alias), v.getValue().getAttribute())
                                ));
                        var subProjection = subQuerySerialization.getColumnIDs().keySet().stream()
                                .filter(v -> variableAliases.containsKey(v))
                                .map(
                                        v -> subQuerySerialization.getColumnIDs().get(v).getSQLRendering() + " AS " + idFactory.createAttributeID(v.getName()).getSQLRendering()
                                )
                                .collect(Collectors.joining(", "));
                        if(subProjection.length() > 0)
                            subProjection += ",";


                        /*We need to run `CASE WHEN RAND() > 1...` here, because otherwise, casting the resulting column to
                        * a different datatype will make the query fail.
                        * We need to add a LIMIT to the end, because otherwise, when accessing a JSON object that is the
                        * result of flatten with square brackets, the access operation will be ignored.
                        * */
                        builder.append(String.format(
                                "(SELECT %s CASE WHEN RAND() > 1 THEN NULL ELSE FLATTEN(%s) END AS %s FROM %s LIMIT 99999999999999) %s",
                                subProjection,
                                expression,
                                allColumnIDs.get(outputVar).getSQLRendering(),
                                subQuerySerialization.getString(),
                                alias
                        ));
                        return new QuerySerializationImpl(
                                builder.toString(),
                                variableAliases
                        );
                    }
                });


    }
}
