package it.unibz.inf.ontop.generation.serializer.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTermType;

@Singleton
public class PostgresSelectFromWhereSerializer extends DefaultSelectFromWhereSerializer implements SelectFromWhereSerializer {

    @Inject
    private PostgresSelectFromWhereSerializer(TermFactory termFactory) {
        super(new DefaultSQLTermSerializer(termFactory) {
            @Override
            protected String castFloatingConstant(String value, DBTermType dbType) {
                return String.format("%s::%s", value, dbType.getCastName());
            }

            @Override
            protected String serializeDatetimeConstant(String datetime, DBTermType dbType) {
                return String.format("CAST(%s AS %s)", serializeStringConstant(datetime), dbType.getCastName());
            }
        });
    }

    @Override
    public SelectFromWhereSerializer.QuerySerialization serialize(SelectFromWhereWithModifiers selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(
                new DefaultSelectFromWhereSerializer.DefaultRelationVisitingSerializer(dbParameters.getQuotedIDFactory()) {
                    /**
                     * https://www.postgresql.org/docs/8.1/queries-limit.html
                     *
                     * [LIMIT { number | ALL }] [OFFSET number]
                     *
                     * If a limit count is given, no more than that many rows will be returned
                     * (but possibly less, if the query itself yields less rows).
                     * LIMIT ALL is the same as omitting the LIMIT clause.
                     *
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
                });
    }
}
