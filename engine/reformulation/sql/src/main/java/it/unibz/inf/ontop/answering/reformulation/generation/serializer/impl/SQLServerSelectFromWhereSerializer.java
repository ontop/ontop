package it.unibz.inf.ontop.answering.reformulation.generation.serializer.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.answering.reformulation.generation.dialect.SQLDialectAdapter;
import it.unibz.inf.ontop.answering.reformulation.generation.serializer.SQLTermSerializer;
import it.unibz.inf.ontop.dbschema.DBParameters;


public class SQLServerSelectFromWhereSerializer extends IgnoreNullFirstSelectFromWhereSerializer {

    @Inject
    private SQLServerSelectFromWhereSerializer(SQLTermSerializer sqlTermSerializer,
                                               SQLDialectAdapter dialectAdapter) {
        super(sqlTermSerializer, dialectAdapter);
    }

    @Override
    public QuerySerialization serialize(SelectFromWhereWithModifiers selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(new IgnoreNullFirstRelationVisitingSerializer(dbParameters.getQuotedIDFactory()) {
            @Override
            protected String serializeDummyTable() {
                // "\"example\"" from SQLAdapter
                return "FROM \"example\"";
            }
        });
    }
}
