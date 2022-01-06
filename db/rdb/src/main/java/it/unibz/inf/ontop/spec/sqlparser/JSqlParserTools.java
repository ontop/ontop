package it.unibz.inf.ontop.spec.sqlparser;

import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.InvalidQueryException;
import it.unibz.inf.ontop.spec.sqlparser.exception.InvalidSelectQueryRuntimeException;
import it.unibz.inf.ontop.spec.sqlparser.exception.UnsupportedSelectQueryRuntimeException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;

public class JSqlParserTools {

    public static SelectBody parse(String sql) throws InvalidQueryException, JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql, parser -> parser.withSquareBracketQuotation(true));
        if (!(statement instanceof Select))
            throw new InvalidQueryException("The query is not a SELECT statement", statement);

        return ((Select) statement).getSelectBody();
    }

    /**
     *
     * @param selectBody
     * @return
     * @throws UnsupportedSelectQueryRuntimeException
     * @throws InvalidSelectQueryRuntimeException
     */

    public static PlainSelect getPlainSelect(SelectBody selectBody) {
        // other subclasses of SelectBody are
        //      SelectOperationList (INTERSECT, EXCEPT, MINUS, UNION),
        //      ValuesStatement (VALUES)
        //      WithItem ([RECURSIVE]...)

        if (!(selectBody instanceof PlainSelect))
            throw new UnsupportedSelectQueryRuntimeException("Complex SELECT statements are not supported", selectBody);

        PlainSelect plainSelect = (PlainSelect) selectBody;

        if (plainSelect.getIntoTables() != null)
            throw new InvalidSelectQueryRuntimeException("SELECT INTO is not allowed in mappings", selectBody);

        return plainSelect;
    }

    public static RelationID getRelationId(QuotedIDFactory idfac, Table table) {
        // a massive workaround for JSQLParser, which supports long names
        // but does NOT give direct access to the components
        if (table.getSchemaName() == null)
            return idfac.createRelationID(table.getName());
        
        if (table.getDatabase().getDatabaseName() == null)
            return idfac.createRelationID(table.getSchemaName(), table.getName());
        
        String s = table.getFullyQualifiedName();
        return idfac.createRelationID(s.split("\\."));
    }
}
