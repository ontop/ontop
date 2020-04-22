package it.unibz.inf.ontop.answering.reformulation.generation.serializer.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.model.term.TermFactory;


@Singleton
public class SQLServerSelectFromWhereSerializer extends IgnoreNullFirstSelectFromWhereSerializer {

    @Inject
    private SQLServerSelectFromWhereSerializer(TermFactory termFactory) {
        super(new DefaultSQLTermSerializer(termFactory));
    }

    @Override
    public QuerySerialization serialize(SelectFromWhereWithModifiers selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(new IgnoreNullFirstRelationVisitingSerializer(dbParameters.getQuotedIDFactory()) {

            //@Override SQL Server allows no FROM clause
            //protected String serializeDummyTable() {
            //    // "\"example\"" from SQLAdapter
            //    return "";
            //}

            /**
             *  https://docs.microsoft.com/en-us/sql/t-sql/queries/select-order-by-clause-transact-sql?view=sql-server-ver15
             *
             * <offset_fetch> ::=
             * {
             *     OFFSET { integer_constant | offset_row_count_expression } { ROW | ROWS }
             *     [
             *       FETCH { FIRST | NEXT } {integer_constant | fetch_row_count_expression } { ROW | ROWS } ONLY
             *     ]
             * }
             */

            @Override
            protected String serializeLimitOffset(long limit, long offset) {
                return String.format("OFFSET %d ROWS\nFETCH NEXT %d ROWS ONLY", offset, limit);
            }

            @Override
            protected String serializeLimit(long limit) {
                return String.format("OFFSET 0 ROWS\nFETCH NEXT %d ROWS ONLY", limit);
            }

            @Override
            protected String serializeOffset(long offset) {
                return String.format("OFFSET %d ROWS", offset);
            }
        });
    }
}
