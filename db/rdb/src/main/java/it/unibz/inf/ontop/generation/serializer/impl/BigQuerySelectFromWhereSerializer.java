package it.unibz.inf.ontop.generation.serializer.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.generation.algebra.SQLFlattenExpression;
import it.unibz.inf.ontop.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.injection.OntopSQLCoreSettings;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Optional;

@Singleton
public class BigQuerySelectFromWhereSerializer extends DefaultSelectFromWhereSerializer implements SelectFromWhereSerializer {

    @Inject
    private BigQuerySelectFromWhereSerializer(TermFactory termFactory, OntopSQLCoreSettings settings) {
        super(new DefaultSQLTermSerializer(termFactory) {
            @Override
            protected String serializeDatetimeConstant(String datetime, DBTermType dbType) {
                return String.format("TIMESTAMP %s", serializeStringConstant(datetime));
            }
        }, settings);
    }

    @Override
    public QuerySerialization serialize(SelectFromWhereWithModifiers selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(
                new DefaultRelationVisitingSerializer(dbParameters.getQuotedIDFactory()) {

                    private static final String VIEW_PREFIX = "r";

                    @Override
                    protected String serializeLimitOffset(long limit, long offset, boolean noSortCondition) {
                        return String.format("LIMIT %d OFFSET %d", limit, offset);
                    }

                    @Override
                    protected String serializeOffset(long offset, boolean noSortCondition) {
                        //OFFSET is not available without LIMIT, so we add a very large LIMIT
                        return String.format("LIMIT 9999999999 OFFSET %d", offset);
                    }

                    @Override
                    protected RelationID generateFreshViewAlias() {
                        return idFactory.createRelationID(VIEW_PREFIX + viewCounter.incrementAndGet());
                    }

                    @Override
                    protected QuerySerialization serializeFlatten(SQLFlattenExpression sqlFlattenExpression, Variable flattenedVar, Variable outputVar, Optional<Variable> indexVar, DBTermType flattenedType, ImmutableMap<Variable, QualifiedAttributeID> allColumnIDs, QuerySerialization subQuerySerialization) {
                        // SELECT <variables> FROM <subquery> CROSS JOIN UNNEST(<flattenedVariable>) [WITH OFFSET <names>]
                        String string = String.format("%s CROSS JOIN UNNEST(%s) %s %s ",
                                subQuerySerialization.getString(),
                                serializeTerm(flattenedVar, allColumnIDs),
                                serializeTerm(outputVar, allColumnIDs),
                                serializeOptionalTerm("WITH OFFSET %s", indexVar, allColumnIDs));

                        return new QuerySerializationImpl(
                                string,
                                getFlattenAllColumnIDs(flattenedVar, allColumnIDs),
                                subQuerySerialization.getCTEMap());
                    }
                });
    }
}
