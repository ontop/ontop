package it.unibz.inf.ontop.generation.serializer.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.db.impl.NullIgnoringDBGroupConcatFunctionSymbol;
import it.unibz.inf.ontop.model.type.DBTermType;

@Singleton
public class DremioSelectFromWhereSerializer extends DefaultSelectFromWhereSerializer implements SelectFromWhereSerializer {

    @Inject
    private DremioSelectFromWhereSerializer(TermFactory termFactory) {
        super(new DefaultSQLTermSerializer(termFactory) {
            @Override
            public String serialize(ImmutableTerm term, ImmutableMap<Variable, QualifiedAttributeID> columnIDs) {
                if(term instanceof ImmutableFunctionalTerm &&
                        ((ImmutableFunctionalTerm) term).getFunctionSymbol() instanceof NullIgnoringDBGroupConcatFunctionSymbol){
                    throw new UnsupportedOperationException("GROUP_CONCAT or LIST_AGG not yet supported by Dremio");
                }
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
