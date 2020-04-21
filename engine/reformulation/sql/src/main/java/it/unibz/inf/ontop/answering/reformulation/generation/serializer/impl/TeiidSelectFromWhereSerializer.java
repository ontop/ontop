package it.unibz.inf.ontop.answering.reformulation.generation.serializer.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.answering.reformulation.generation.dialect.SQLDialectAdapter;
import it.unibz.inf.ontop.answering.reformulation.generation.serializer.SQLTermSerializer;
import it.unibz.inf.ontop.answering.reformulation.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.dbschema.DBParameters;

public class TeiidSelectFromWhereSerializer extends DefaultSelectFromWhereSerializer implements SelectFromWhereSerializer {

    @Inject
    private TeiidSelectFromWhereSerializer(SQLTermSerializer sqlTermSerializer,
                                         SQLDialectAdapter dialectAdapter) {
        super(sqlTermSerializer, dialectAdapter);
    }

    @Override
    public SelectFromWhereSerializer.QuerySerialization serialize(SelectFromWhereWithModifiers
                                                                          selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(
                new DefaultSelectFromWhereSerializer.DefaultRelationVisitingSerializer(dbParameters.getQuotedIDFactory()) {
                    /**
                     * See https://docs.jboss.org/teiid/7.7.0.Final/reference/en-US/html/sql_clauses.html#limit_clause
                     *
                     * LIMIT [offset,] limit
                     * [OFFSET offset ROW|ROWS] [FETCH FIRST|NEXT [limit] ROW|ROWS ONLY
                     *
                     * The limit/offset expressions must be a non-negative integer or a parameter reference (?). An offset of 0 is ignored. A limit of 0 will return no rows.
                     * The terms FIRST/NEXT are interchangable as well as ROW/ROWS.
                     *
                     * LIMIT 100 returns the first 100 records(rows 1-100)
                     * LIMIT 500,100  skips 500 records and returns the next 100 records(rows 501-600)
                     * OFFSET 500 ROWS skips 500 records
                     * OFFSET 500 ROWS FETCH NEXT 100 ROWS ONLY  skips 500 records and returns the next 100 records(rows 501-600)
                     * FETCH FIRST ROW ONLY - returns only the first record
                     */

                    //  serializeLimitOffset and  serializeLimit are standard

                    @Override
                    protected String serializeOffset(long offset) {
                        return String.format("OFFSET %d ROWS", offset);
                    }
                });
    }
}
