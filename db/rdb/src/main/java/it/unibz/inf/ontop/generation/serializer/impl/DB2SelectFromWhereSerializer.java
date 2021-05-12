package it.unibz.inf.ontop.generation.serializer.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.model.term.TermFactory;

@Singleton
public class DB2SelectFromWhereSerializer extends IgnoreNullFirstSelectFromWhereSerializer {

    @Inject
    private DB2SelectFromWhereSerializer(TermFactory termFactory) {
        super(new DefaultSQLTermSerializer(termFactory));
    }

    @Override
    public QuerySerialization serialize(SelectFromWhereWithModifiers selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(new IgnoreNullFirstRelationVisitingSerializer(dbParameters.getQuotedIDFactory()) {
            @Override
            protected String serializeDummyTable() {
                return "FROM sysibm.sysdummy1";
            }

            // serializeLimit is standard

            @Override
            protected String serializeLimitOffset(long limit, long offset, boolean noSortCondition) {
                return String.format("LIMIT %d\nOFFSET %d", limit, offset);
            }

            @Override
            protected String serializeOffset(long offset, boolean noSortCondition) {
                return serializeLimitOffset(8000, offset, noSortCondition);
            }
        });
    }
}
