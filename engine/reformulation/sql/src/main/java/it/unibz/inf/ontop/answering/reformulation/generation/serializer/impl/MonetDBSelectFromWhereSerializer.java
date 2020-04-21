package it.unibz.inf.ontop.answering.reformulation.generation.serializer.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.answering.reformulation.generation.dialect.SQLDialectAdapter;
import it.unibz.inf.ontop.answering.reformulation.generation.serializer.SQLTermSerializer;
import it.unibz.inf.ontop.answering.reformulation.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.dbschema.DBParameters;

public class MonetDBSelectFromWhereSerializer extends DefaultSelectFromWhereSerializer implements SelectFromWhereSerializer {

    @Inject
    private MonetDBSelectFromWhereSerializer(SQLTermSerializer sqlTermSerializer,
                                              SQLDialectAdapter dialectAdapter) {
        super(sqlTermSerializer, dialectAdapter);
    }

    @Override
    public SelectFromWhereSerializer.QuerySerialization serialize(SelectFromWhereWithModifiers
                                                                          selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(
                new DefaultSelectFromWhereSerializer.DefaultRelationVisitingSerializer(dbParameters.getQuotedIDFactory()) {
                    /**
                     * https://www.monetdb.org/Documentation/SQLreference/TableExpressions
                     *  [ LIMIT posint ]
                     *  [ OFFSET posint ]
                     */

                    // sqlLimit, sqlOffset, sqlTopNSQL are standard

                    @Override
                    protected String serializeLimitOffset(long limit, long offset) {
                        return String.format("LIMIT %d\nOFFSET %d", offset, limit);
                    }
                });
    }

}

