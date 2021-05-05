package it.unibz.inf.ontop.generation.serializer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.generation.algebra.SQLOneTupleDummyQueryExpression;
import it.unibz.inf.ontop.generation.algebra.SQLOrderComparator;
import it.unibz.inf.ontop.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;


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
            protected String serializeLimitOffset(long limit, long offset, boolean noSortCondition) {
                return noSortCondition
                        ? String.format("ORDER BY (SELECT NULL)\nOFFSET %d ROWS\nFETCH NEXT %d ROWS ONLY", offset, limit)
                        : String.format("OFFSET %d ROWS\nFETCH NEXT %d ROWS ONLY", offset, limit);
            }

            /**
             * LIMIT without ORDER BY not supported in SQLServer
             * ORDER BY (SELECT NULL) added as a default when no ORDER BY present
             */
            @Override
            protected String serializeLimit(long limit, boolean noSortCondition) {
                return noSortCondition
                        ? String.format("ORDER BY (SELECT NULL)\nOFFSET 0 ROWS\nFETCH NEXT %d ROWS ONLY", limit)
                        : String.format("OFFSET 0 ROWS\nFETCH NEXT %d ROWS ONLY", limit);
            }

            @Override
            protected String serializeOffset(long offset, boolean noSortCondition) {
                return noSortCondition
                        ? String.format("ORDER BY (SELECT NULL)\nOFFSET %d ROWS", offset)
                        : String.format("OFFSET %d ROWS", offset);
            }

            @Override
            public QuerySerialization visit(SQLOneTupleDummyQueryExpression sqlOneTupleDummyQueryExpression) {
                String fromString = serializeDummyTable();
                String sqlSubString = String.format("(SELECT 1 AS dummyVarSQLServer %s) tdummy", fromString);
                return new QuerySerializationImpl(sqlSubString, ImmutableMap.of());
            }
        });
    }
}
