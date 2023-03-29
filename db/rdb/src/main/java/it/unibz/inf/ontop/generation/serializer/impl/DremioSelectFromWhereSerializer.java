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
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.impl.NullIgnoringDBGroupConcatFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;

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

                    private String serializeValuesEntry(Constant constant, ImmutableMap<Variable, QualifiedAttributeID> childColumnIDs) {
                        String serialization = sqlTermSerializer.serialize(constant, childColumnIDs);
                        if(constant instanceof DBConstant && ((DBConstant) constant).getType().getCategory() == DBTermType.Category.INTEGER) {
                            return String.format("CAST(%s as INTEGER)", serialization);
                        }
                        return serialization;
                    }

                    //Due to a limitation of dremio, we need to cast integer constants in VALUES terms to integers.
                    @Override
                    public QuerySerialization visit(SQLValuesExpression sqlValuesExpression) {
                        ImmutableList<Variable> orderedVariables = sqlValuesExpression.getOrderedVariables();
                        ImmutableMap<Variable, QuotedID> variableAliases = createVariableAliases(ImmutableSet.copyOf(orderedVariables));
                        // Leaf node
                        ImmutableMap<Variable, QualifiedAttributeID> childColumnIDs = ImmutableMap.of();

                        String tuplesSerialized = sqlValuesExpression.getValues().stream()
                                .map(tuple -> tuple.stream()
                                        .map(constant -> serializeValuesEntry(constant, childColumnIDs))
                                        .collect(Collectors.joining(",", " (", ")")))
                                .collect(Collectors.joining(","));
                        RelationID alias = generateFreshViewAlias();
                        String internalColumnNames = orderedVariables.stream()
                                .map(variableAliases::get)
                                .map(QuotedID::toString)
                                .collect(Collectors.joining(",", " (", ")"));
                        String sql = "(VALUES " + tuplesSerialized + ") AS " + alias + internalColumnNames;

                        ImmutableMap<Variable, QualifiedAttributeID> columnIDs = attachRelationAlias(alias, variableAliases);

                        return new QuerySerializationImpl(sql, columnIDs);
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
                });
    }
}
