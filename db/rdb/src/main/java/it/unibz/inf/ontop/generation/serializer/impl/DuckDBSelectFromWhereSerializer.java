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

                    @Override
                    protected QuerySerialization serializeFlatten(SQLFlattenExpression sqlFlattenExpression, Variable flattenedVar, Variable outputVar, Optional<Variable> indexVar, DBTermType flattenedType, ImmutableMap<Variable, QualifiedAttributeID> allColumnIDs, QuerySerialization subQuerySerialization) {
                        //We express the flatten call as a `SELECT *, UNNEST({array}) FROM child.
                        var expression = allColumnIDs.get(flattenedVar).getSQLRendering();

                        //If an index is required, we use create a second list which is an integer range from 1 to len(list) and unnset it, too.
                        String flattenCall;
                        if(indexVar.isPresent()) {
                            flattenCall = String.format("UNNEST(%s) AS %s, UNNEST(RANGE(1, len(%s) + 1)) as %s",
                                    expression,
                                    allColumnIDs.get(outputVar).getSQLRendering(),
                                    expression,
                                    allColumnIDs.get(indexVar.get()).getSQLRendering());
                        } else {
                            flattenCall = String.format("(UNNEST(%s)) AS %s",
                                    expression,
                                    allColumnIDs.get(outputVar).getSQLRendering());
                        }
                        return serializeFlattenAsFunction(flattenedVar, allColumnIDs, subQuerySerialization, flattenCall);
                    }
                });
    }
}
