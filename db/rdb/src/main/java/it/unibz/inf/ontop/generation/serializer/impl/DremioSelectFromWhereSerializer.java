package it.unibz.inf.ontop.generation.serializer.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.impl.NullIgnoringDBGroupConcatFunctionSymbol;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

import java.util.Optional;
import java.util.stream.Collectors;

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
        });
    }

    @Override
    public QuerySerialization serialize(SelectFromWhereWithModifiers selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(
                new DefaultRelationVisitingSerializer(dbParameters.getQuotedIDFactory()) {
                    /**
                     * Dremio does not support the syntax:
                     * SELECT NULL as "v0"
                     * but accepts an expression that evaluates to NULL instead.
                     * E.g.:
                     * SELECT NULL = NULL as "v0"
                     * is valid.
                     * This cannot be generalized to all occurrences of NULL though.
                     * E.g:
                     * SELECT DISTINCT CASE WHEN (1 = 0) THEN 0 ELSE NULL END AS "v0"
                     * is syntactically valid, but:
                     * SELECT DISTINCT CASE WHEN (1 = 0) THEN 0 ELSE (NULL = NULL) END AS "v0"
                     * is not
                     */
                    @Override
                    protected String serializeProjection(ImmutableSortedSet<Variable> projectedVariables,
                                                         ImmutableMap<Variable, QuotedID> variableAliases,
                                                         ImmutableSubstitution<? extends ImmutableTerm> substitution,
                                                         ImmutableMap<Variable, QualifiedAttributeID> columnIDs) {

                        if (projectedVariables.isEmpty())
                            return "1 AS uselessVariable";

                        return projectedVariables.stream()
                                .map(v -> serializeDef(
                                        v,
                                        Optional.ofNullable((ImmutableTerm)substitution.get(v)).orElse(v),
                                        columnIDs,
                                        variableAliases
                                ))
                                .collect(Collectors.joining(", "));
                    }

                    private String serializeDef(Variable v, ImmutableTerm term, ImmutableMap<Variable, QualifiedAttributeID> columnIDs, ImmutableMap<Variable, QuotedID> variableAliases) {
                        return serializeValue(term, columnIDs) + " AS " + variableAliases.get(v).getSQLRendering();
                    }

                    private String serializeValue(ImmutableTerm term, ImmutableMap<Variable, QualifiedAttributeID> columnIDs) {
                        return term.isNull()?
                                "(NULL = NULL)":
                                sqlTermSerializer.serialize(
                                    term,
                                    columnIDs
                                );
                    }
                });
    }
}
