package it.unibz.inf.ontop.generation.serializer.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.generation.algebra.SQLFlattenExpression;
import it.unibz.inf.ontop.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.generation.serializer.SQLSerializationException;
import it.unibz.inf.ontop.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.injection.OntopSQLCoreSettings;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Optional;
import java.util.stream.Stream;

@Singleton
public class DremioSelectFromWhereSerializer extends DefaultSelectFromWhereSerializer implements SelectFromWhereSerializer {

    @Inject
    private DremioSelectFromWhereSerializer(TermFactory termFactory, OntopSQLCoreSettings settings) {
        super(new DefaultSQLTermSerializer(termFactory) {
            @Override
            public String serialize(ImmutableTerm term, ImmutableMap<Variable, QualifiedAttributeID> columnIDs) {
                return super.serialize(term,columnIDs);
            }

            @Override
            protected String serializeDatetimeConstant(String datetime, DBTermType dbType) {
                return String.format("CAST(%s AS %s)", serializeStringConstant(datetime), dbType.getCastName());
            }
        }, settings);
    }

    @Override
    public QuerySerialization serialize(SelectFromWhereWithModifiers selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(
                new DefaultRelationVisitingSerializer(dbParameters.getQuotedIDFactory()) {
                    @Override
                    protected String serializeLimitOffset(long limit, long offset, boolean noSortCondition) {
                        return String.format("LIMIT %d OFFSET %d", limit, offset);
                    }

                    //Due to a limitation of dremio, we need to cast integer constants in VALUES terms to integers, as they would be types as int64 otherwise.
                    @Override
                    protected String serializeValuesEntry(Constant constant) {
                        String serialization = super.serializeValuesEntry(constant);
                        if (constant instanceof DBConstant && ((DBConstant) constant).getType().getCategory() == DBTermType.Category.INTEGER) {
                            return String.format("CAST(%s as INTEGER)", serialization);
                        }
                        return serialization;
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


                    @Override
                    protected QuerySerialization serializeFlatten(SQLFlattenExpression sqlFlattenExpression, Variable flattenedVar, Variable outputVar, Optional<Variable> indexVar, DBTermType flattenedType, ImmutableMap<Variable, QualifiedAttributeID> allColumnIDs, QuerySerialization subQuerySerialization) {
                        if (indexVar.isPresent()) {
                            throw new SQLSerializationException("Dremio does not support FLATTEN with position arguments.");
                        }

                        //We express the flatten call as a `SELECT *, FLATTEN({array}) FROM child.

                        //FLATTEN only works on ARRAY<T> types, so we first transform the JSON-array into an ARRAY<STRING> if it is not already one
                        var expression = flattenedType.getCategory() == DBTermType.Category.ARRAY
                                ? serializeTerm(flattenedVar, allColumnIDs)
                                : String.format("CONVERT_FROM(%s, 'json')", serializeTerm(flattenedVar, allColumnIDs));

                        // We need to run `CASE WHEN RAND() > 1...` here, because otherwise, casting the resulting column to
                        //a different datatype will make the query fail.
                        return serializeFlattenAsSubQuery(flattenedVar, allColumnIDs, subQuerySerialization,
                                Stream.of(serializeColumnAlias(
                                        String.format("CASE WHEN RAND() > 1 THEN NULL ELSE FLATTEN(%s) END", expression),
                                        serializeTerm(outputVar, allColumnIDs))));
                    }

                    /*
                     * We need to add a LIMIT to the end, because otherwise, when accessing a JSON object that is the
                     * result of flatten with square brackets, the access operation will be ignored.
                     */
                    @Override
                    protected String getFlattenSubQueryTemplate() {
                        return "(SELECT %s FROM %s LIMIT 999999999) %s";
                    }
                });
    }
}
