package it.unibz.inf.ontop.answering.reformulation.generation.serializer.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.answering.reformulation.generation.dialect.SQLDialectAdapter;
import it.unibz.inf.ontop.answering.reformulation.generation.serializer.SQLTermSerializer;
import it.unibz.inf.ontop.dbschema.DBParameters;

@Singleton
public class DB2SelectFromWhereSerializer extends IgnoreNullFirstSelectFromWhereSerializer {

    @Inject
    private DB2SelectFromWhereSerializer(SQLTermSerializer sqlTermSerializer,
                                                     SQLDialectAdapter dialectAdapter) {
        super(sqlTermSerializer, dialectAdapter);
    }

    @Override
    public QuerySerialization serialize(SelectFromWhereWithModifiers selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(new IgnoreNullFirstRelationVisitingSerializer(dbParameters.getQuotedIDFactory()) {
            @Override
            protected String serializeDummyTable() {
                return "FROM sysibm.sysdummy1";
            }

            // serializeLimit

            @Override
            protected String serializeLimitOffset(long limit, long offset) {
                return String.format("LIMIT %d\nOFFSET %d", limit, offset);
            }

            @Override
            protected String serializeOffset(long offset) {
                return serializeLimitOffset(8000, offset);
            }
        });
    }
}
