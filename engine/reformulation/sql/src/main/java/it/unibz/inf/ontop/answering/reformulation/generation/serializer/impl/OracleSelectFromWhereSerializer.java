package it.unibz.inf.ontop.answering.reformulation.generation.serializer.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.answering.reformulation.generation.dialect.SQLDialectAdapter;
import it.unibz.inf.ontop.answering.reformulation.generation.serializer.SQLTermSerializer;
import it.unibz.inf.ontop.dbschema.DBParameters;

public class OracleSelectFromWhereSerializer extends DefaultSelectFromWhereSerializer {

    @Inject
    private OracleSelectFromWhereSerializer(SQLTermSerializer sqlTermSerializer,
                                         SQLDialectAdapter dialectAdapter) {
        super(sqlTermSerializer, dialectAdapter);
    }

    @Override
    public QuerySerialization serialize(SelectFromWhereWithModifiers selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(new DefaultRelationVisitingSerializer(dbParameters.getQuotedIDFactory()) {
            @Override
            protected String serializeDummyTable() {
                return "FROM dual";
            }

            /**
             * Versions < 12.1 are not supported
             * Reason: In 12.1 and later, you can use the OFFSET and/or FETCH [FIRST | NEXT] operators
             */
            @Override
            protected String serializeLimitOffset(long limit, long offset) {
                return String.format("OFFSET %d ROWS\nFETCH NEXT %d ROWS ONLY", offset, limit);
            }

            @Override
            protected String serializeLimit(long limit) {
                // ROWNUM <= limit could also be used
                return String.format("FETCH NEXT %d ROWS ONLY", limit);
            }

            @Override
            protected String serializeOffset(long offset) {
                return String.format("OFFSET %d ROWS\nFETCH NEXT 99999999 ROWS ONLY", offset);
            }

        });
    }
}
