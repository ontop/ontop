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

                    @Override
                    protected QuerySerialization serializeFlatten(SQLFlattenExpression sqlFlattenExpression, Variable flattenedVar, Variable outputVar, Optional<Variable> indexVar, DBTermType flattenedType, ImmutableMap<Variable, QualifiedAttributeID> allColumnIDs, QuerySerialization subQuerySerialization) {
                        //We build the query string of the form SELECT <variables> FROM <subquery> CROSS JOIN UNNEST(<flattenedVariable>) WITH ORDINALITY AS <names>
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

                    private String getOutputVarsRendering(Variable outputVar, Optional<Variable> indexVar, ImmutableMap<Variable, QualifiedAttributeID> allColumnIDs) {
                        String outputVarString = allColumnIDs.get(outputVar).getSQLRendering();
                        RelationID viewAlias = generateFreshViewAlias();

                        return String.format(
                                        "%s(%s%s)",
                                        viewAlias.getSQLRendering(),
                                        outputVarString,
                                        indexVar.isPresent() ? ", " + allColumnIDs.get(indexVar.get()).getSQLRendering() : "");
                    }
                });
    }
}
