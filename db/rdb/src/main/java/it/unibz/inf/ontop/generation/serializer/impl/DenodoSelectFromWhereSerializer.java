package it.unibz.inf.ontop.generation.serializer.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.model.term.TermFactory;

@Singleton
public class DenodoSelectFromWhereSerializer extends DefaultSelectFromWhereSerializer implements SelectFromWhereSerializer {

    @Inject
    private DenodoSelectFromWhereSerializer(TermFactory termFactory) {
        super(new DefaultSQLTermSerializer(termFactory));
    }

    @Override
    public SelectFromWhereSerializer.QuerySerialization serialize(SelectFromWhereWithModifiers selectFromWhere, DBParameters dbParameters) {
        return selectFromWhere.acceptVisitor(
                new DefaultSelectFromWhereSerializer.DefaultRelationVisitingSerializer(dbParameters.getQuotedIDFactory()) {
                    /**
                     * https://community.denodo.com/docs/html/browse/6.0/vdp/vql/queries_select_statement/offset_fetch_and_limit/offset_fetch_and_limit
                     *
                     * he OFFSET, FETCH and LIMIT clauses limit the number of rows obtained when executing a query.
                     * Use OFFSET <number> [ ROW | ROWS ] to skip the first n rows of the result set.
                     * Use LIMIT [ <count> ] or FETCH { FIRST | NEXT } [ <count> ] { ROW | ROWS } ONLY to obtain
                     * only <count> rows of the result set.
                     *
                     * The parameters ROW and ROWS have the same meaning and can be used indistinctly.
                     * FIRST and NEXT can also be used indistinctly.
                     * For consistent results, the query must ensure a deterministic sort order.
                     * You can use OFFSET combined with LIMIT or FETCH (see the syntax of these clauses in the Syntax of the SELECT statement).
                     *
                     * Example 1
                     * SELECT f1, f2 FROM employee FETCH FIRST 10 ROWS ONLY
                     * Executes the query and returns the first ten rows of the result set.
                     *
                     * Example 2
                     * SELECT f1, f2 FROM employee OFFSET 10 ROWS FETCH NEXT 10 ROWS ONLY
                     * Executes the query and returns the rows number 10 to number 19 (both included). The first row is row number 0.
                     *
                     * The following query uses LIMIT and is equivalent to the previous one:
                     * SELECT f1, f2 FROM employee OFFSET 10 ROWS LIMIT 10
                     *
                     * Example 3
                     * If you use FETCH without <count>, the Server only returns one row.
                     * For example, the following query only returns the first row of the result set:
                     * SELECT f1, f2 FROM employee FETCH NEXT ROW ONLY
                     */

                    // serializeLimit is standard

                    @Override
                    public String serializeLimitOffset(long limit, long offset) {
                        return String.format("OFFSET %d ROWS\nFETCH NEXT %d ROWS ONLY", offset, limit);
                    }

                    @Override
                    protected String serializeOffset(long offset) {
                        return String.format("OFFSET %d ROWS", offset);
                    }
                });
    }
}
