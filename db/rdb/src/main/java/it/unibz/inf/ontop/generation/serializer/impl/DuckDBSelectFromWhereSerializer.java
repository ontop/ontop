package it.unibz.inf.ontop.generation.serializer.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.generation.algebra.SQLFlattenExpression;
import it.unibz.inf.ontop.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class DuckDBSelectFromWhereSerializer extends DefaultSelectFromWhereSerializer implements SelectFromWhereSerializer {

    @Inject
    private DuckDBSelectFromWhereSerializer(TermFactory termFactory) {
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

                    @Override
                    protected String serializeLimitOffset(long limit, long offset, boolean noSortCondition) {
                        return String.format("OFFSET %d LIMIT %d", offset, limit);
                    }

                    @Override
                    protected String serializeOffset(long offset, boolean noSortCondition) {
                        return String.format("OFFSET %d", offset);
                    }

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

                        //We express the flatten call as a `SELECT *, UNNEST({array}) FROM child.
                        var expression = allColumnIDs.get(flattenedVar).getSQLRendering();

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

                        //If an index is required, we use create a second list which is an integer range from 1 to len(list) and unnset it, too.
                        if(indexVar.isPresent()) {
                            builder.append(String.format(
                                    "(SELECT %s UNNEST(%s) AS %s, UNNEST(RANGE(1, len(%s) + 1)) as %s FROM %s) %s",
                                    subProjection,
                                    expression,
                                    allColumnIDs.get(outputVar).getSQLRendering(),
                                    expression,
                                    allColumnIDs.get(indexVar.get()).getSQLRendering(),
                                    subQuerySerialization.getString(),
                                    alias

                            ));
                        } else {
                            builder.append(String.format(
                                    "(SELECT %s (UNNEST(%s)) AS %s FROM %s) %s",
                                    subProjection,
                                    expression,
                                    allColumnIDs.get(outputVar).getSQLRendering(),
                                    subQuerySerialization.getString(),
                                    alias

                            ));
                        }
                        return new QuerySerializationImpl(
                                builder.toString(),
                                variableAliases
                        );
                    }

                });
    }
}
