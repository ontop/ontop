package it.unibz.inf.ontop.answering.reformulation.generation.serializer.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.answering.reformulation.generation.dialect.SQLDialectAdapter;
import it.unibz.inf.ontop.answering.reformulation.generation.serializer.SQLTermSerializer;
import it.unibz.inf.ontop.answering.reformulation.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.dbschema.DBParameters;

public class H2SelectFromWhereSerializer extends DefaultSelectFromWhereSerializer implements SelectFromWhereSerializer {

    @Inject
    private H2SelectFromWhereSerializer(SQLTermSerializer sqlTermSerializer,
                                             SQLDialectAdapter dialectAdapter) {
        super(sqlTermSerializer, dialectAdapter);
    }

    @Override
    public SelectFromWhereSerializer.QuerySerialization serialize(SelectFromWhereWithModifiers
                                                                          selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(
                new DefaultSelectFromWhereSerializer.DefaultRelationVisitingSerializer(dbParameters.getQuotedIDFactory()) {
                    /**
                     * Number of rows in output can be limited either with standard OFFSET / FETCH,
                     * with non-standard LIMIT / OFFSET, or with non-standard TOP clauses.
                     * Different clauses cannot be used together. OFFSET specifies how many rows to skip.
                     * Please note that queries with high offset values can be slow.
                     * FETCH FIRST/NEXT, LIMIT or TOP limits the number of rows returned by the query
                     * (no limit if null or smaller than zero). If PERCENT is specified number of rows
                     * is specified as a percent of the total number of rows and should be an integer
                     * value between 0 and 100 inclusive. WITH TIES can be used only together with
                     * ORDER BY and means that all additional rows that have the same sorting position
                     * as the last row will be also returned.
                     *
                     * LIMIT expression [OFFSET expression]
                     * OFFSET expression ROW|ROWS FETCH FIRST|(NEXT expression) [PERCENT]
                     */

                    // serializeLimit is standard

                    @Override
                    protected String serializeLimitOffset(long limit, long offset) {
                        if (limit == 0)
                            return "LIMIT 0";

                        return String.format("OFFSET %d ROWS\nFETCH NEXT %d ROWS ONLY", offset, limit);
                    }

                    @Override
                    protected String serializeOffset(long offset) {
                        return String.format("OFFSET %d ROWS", offset);
                    }
                });
    }
}
