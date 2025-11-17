package it.unibz.inf.ontop.generation.serializer.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.generation.algebra.SQLFlattenExpression;
import it.unibz.inf.ontop.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.injection.OntopSQLCoreSettings;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Optional;
import java.util.stream.Stream;

@Singleton
public class DuckDBSelectFromWhereSerializer extends DefaultSelectFromWhereSerializer implements SelectFromWhereSerializer {

    @Inject
    private DuckDBSelectFromWhereSerializer(TermFactory termFactory, OntopSQLCoreSettings settings) {
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

                    @Override
                    protected String serializeLimitOffset(long limit, long offset, boolean noSortCondition) {
                        return String.format("OFFSET %d LIMIT %d", offset, limit);
                    }

                    @Override
                    protected String serializeOffset(long offset, boolean noSortCondition) {
                        return String.format("OFFSET %d", offset);
                    }

                    @Override
                    protected QuerySerialization serializeFlatten(SQLFlattenExpression sqlFlattenExpression, Variable flattenedVar, Variable outputVar, Optional<Variable> indexVar, DBTermType flattenedType, ImmutableMap<Variable, QualifiedAttributeID> allColumnIDs, QuerySerialization subQuerySerialization) {
                        //We express the flatten call as a `SELECT *, UNNEST({array}) FROM child.

                        //If an index is required, we use create a second list which is an integer range from 1 to len(list) and unnset it, too.
                        String flattenCall = indexVar.isPresent()
                           ? String.format("UNNEST(%1$s) AS %2$s, UNNEST(RANGE(1, len(%1$s) + 1)) AS %3$s",
                                    serializeTerm(flattenedVar, allColumnIDs),
                                    serializeTerm(outputVar, allColumnIDs),
                                    serializeTerm(indexVar.get(), allColumnIDs))
                           :  serializeColumnAlias(
                                    String.format("(UNNEST(%s))", serializeTerm(flattenedVar, allColumnIDs)),
                                    serializeTerm(outputVar, allColumnIDs));

                        return serializeFlattenAsSubQuery(flattenedVar, allColumnIDs, subQuerySerialization, Stream.of(flattenCall));
                    }
                });
    }
}
