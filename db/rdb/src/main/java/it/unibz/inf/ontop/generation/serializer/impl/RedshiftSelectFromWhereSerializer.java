package it.unibz.inf.ontop.generation.serializer.impl;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.generation.algebra.SQLFlattenExpression;
import it.unibz.inf.ontop.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;

public class RedshiftSelectFromWhereSerializer extends PostgresSelectFromWhereSerializer {

    @Inject
    protected RedshiftSelectFromWhereSerializer(TermFactory termFactory) {
        super(termFactory);
    }

    @Override
    public SelectFromWhereSerializer.QuerySerialization serialize(SelectFromWhereWithModifiers selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(
                new DefaultSelectFromWhereSerializer.DefaultRelationVisitingSerializer(dbParameters.getQuotedIDFactory()) {
                    /**
                     * https://www.postgresql.org/docs/8.1/queries-limit.html
                     * <p>
                     * [LIMIT { number | ALL }] [OFFSET number]
                     * <p>
                     * If a limit count is given, no more than that many rows will be returned
                     * (but possibly less, if the query itself yields less rows).
                     * LIMIT ALL is the same as omitting the LIMIT clause.
                     * <p>
                     * OFFSET says to skip that many rows before beginning to return rows.
                     * OFFSET 0 is the same as omitting the OFFSET clause. If both OFFSET and LIMIT
                     * appear, then OFFSET rows are skipped before starting to count the LIMIT rows
                     * that are returned.
                     */

                    // serializeLimit and serializeOffset are standard
                    @Override
                    protected String serializeLimitOffset(long limit, long offset, boolean noSortCondition) {
                        return String.format("LIMIT %d\nOFFSET %d", limit, offset);
                    }

                    @Override
                    protected QuerySerialization serializeFlatten(SQLFlattenExpression sqlFlattenExpression,
                                                                  Variable flattenedVar, Variable outputVar,
                                                                  Optional<Variable> indexVar, DBTermType flattenedType,
                                                                  ImmutableMap<Variable, QualifiedAttributeID> allColumnIDs,
                                                                  QuerySerialization subQuerySerialization) {
                        //We now build the query string of the form SELECT <variables> FROM <subquery> JOIN LATERAL <flatten_function>(<flattenedVariable>) WITH ORDINALITY AS <name>
                        StringBuilder builder = new StringBuilder();

                        builder.append(
                                String.format(
                                        "%s, %s AS %s %s",
                                        subQuerySerialization.getString(),
                                        allColumnIDs.get(flattenedVar).getSQLRendering(),
                                        allColumnIDs.get(outputVar).getSQLRendering(),
                                        indexVar.map(v -> String.format(" AT %s",
                                                        allColumnIDs.get(v).getSQLRendering()))
                                                .orElse("")
                                ));

                        return new QuerySerializationImpl(
                                builder.toString(),
                                allColumnIDs.entrySet().stream()
                                        .filter(e -> e.getKey() != flattenedVar)
                                        .collect(ImmutableCollectors.toMap())
                        );
                    }
                });
    }
}
